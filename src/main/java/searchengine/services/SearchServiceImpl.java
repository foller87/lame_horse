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
    public ResponseEntity search(String query, String siteUrl, int offset, int limit) {
        log.info("Поисковый запрос - " + query);
        Map<String, Object> response = new HashMap<>();
        if (query.trim().isBlank()) {
            response.put("result", false);
            response.put("error", "Задан пустой поисковый запрос");
            return ResponseEntity.ok(response);
        }
        List<Site> sites = getUrlListBySiteUrl(siteUrl);
        List<Lemma> lemmasSortedAndFilter = lemmaService.getLemmasByQuery(query);
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
        return ResponseEntity.ok(buildResponse(pages, lemmasSortedAndFilter, limit, offset));
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
    private SearchResponse buildResponse(List<Page> pages, List<Lemma> lemmasSortedAndFilter, int limit, int offset) {
        data = new TreeSet<>(Comparator.comparingDouble(Data::getRelevance).reversed());
        pages.forEach(page -> {
            Data pageData = new Data();
            pageData.setSite(page.getSite().getUrl());
            pageData.setSiteName(page.getSite().getName());
            pageData.setUri(page.getPath().substring(pageData.getSite().length() + 3));
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
        List<Data> dataList = new LinkedList<>();
        data.forEach(d->dataList.add(d));
        if (dataList.size() > limit) dataList.subList(offset, limit);
    return SearchResponse.builder()
                        .result(true)
                        .count(pages.size())
                        .data(dataList).build();
    }
}
