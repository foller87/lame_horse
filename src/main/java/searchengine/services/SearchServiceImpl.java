package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.search.Data;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.SiteRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService{

    private final LemmaService lemmaService;
    private final PageService pageService;
    private final SiteRepository siteRepository;
    private Set<Data> data;

    @Override
    public ResponseEntity search(String query, String siteUrl, long limit, long offset) {
        log.info("Поисковый запрос - " + query);
        Map<String, Object> response = new HashMap<>();
        if (query.trim().isBlank()) {
            response.put("result", false);
            response.put("error", "Задан пустой поисковый запрос");
            return ResponseEntity.ok(response);
        }
        List<Site> sites = getUrlListBySiteUrl(siteUrl);
        List<Lemma> lemmasSortedAndFilter = new ArrayList<>();
        lemmasSortedAndFilter = lemmaService.getLemmasByQuery(query);
        log.info("Искомые леммы - " + lemmasSortedAndFilter.toString());
        if (lemmasSortedAndFilter.isEmpty()) {
            response.put("result", false);
            response.put("error", "Список лемм пуст, задайте корректный запрос");
            return ResponseEntity.ok(response);
        }
        List<Page> pages = pageService.getPagesByLemmas(lemmasSortedAndFilter);
        if (pages.isEmpty()) {
            response.put("result", false);
            response.put("error", "По данному запросу не нашлось ни одной страницы страниц");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(buildResponse(pages, lemmasSortedAndFilter));
    }
    private List<Site> getUrlListBySiteUrl(String siteUrl) {
        List<Site> sites = new ArrayList<>();
        if (siteUrl == null) {
            siteRepository.findBySiteStatus(SiteStatus.INDEXED)
                    .forEach(site -> sites.add(site));
        } else {
            sites.add(siteRepository.findByUrl(siteUrl).get(0));
        }
        return sites;
    }
    private SearchResponse buildResponse(List<Page> pages, List<Lemma> lemmasSortedAndFilter) {
        data = new TreeSet<>(Comparator.comparingDouble(Data::getRelevance).reversed());
        pages.forEach(page -> {
            Data pageData = new Data();
            pageData.setSite(page.getSite().getUrl());
            pageData.setSiteName(page.getSite().getName());
            pageData.setUri(page.getPath().substring(page.getPath().indexOf(pageData.getSite()) + pageData.getSite().length()));
            Document doc = Jsoup.parse(page.getContent());
            for (String field : Arrays.asList("title", "body")) {
                Element element = doc.getElementsByTag(field).first();
                if (field.equals("title") && element != null) pageData.setTitle(element.text());
                if (field.equals("body"))
                    pageData.setSnippet(lemmaService.getFragmentText(element.text(),lemmasSortedAndFilter));
            }
            pageData.setRelevance(pageService.getRelativeRelevance(page, pages, lemmasSortedAndFilter));
            data.add(pageData);
        });
    return SearchResponse.builder()
                        .result(true)
                        .count(pages.size())
                        .data(data).build();
    }
}
