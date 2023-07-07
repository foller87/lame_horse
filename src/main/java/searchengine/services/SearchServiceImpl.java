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
import searchengine.model.SiteStatus;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

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
        List<Lemma> lemmasSortedAndFilter = lemmaService.getLemmasByQuery(query);
        log.info("Искомые леммы - " + lemmasSortedAndFilter.toString());
        if (lemmasSortedAndFilter.isEmpty()) {
            response.put("result", false);
            response.put("error", "Список лемм пуст, задайте корректный запрос");
            return ResponseEntity.ok(response);
        }
        List<Page> pages = pageService.getPagesByLemmas(lemmasSortedAndFilter);
        List<Long> sitesId = getUrlListBySiteUrl(siteUrl);
        List<Page> filteredPages = pages.stream().filter(page -> sitesId.contains
                (page.getSite().getId())).collect(Collectors.toList());
        if (filteredPages.isEmpty()) {
            response.put("result", false);
            response.put("error", "По данному запросу не нашлось ни одной страницы страниц");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(buildResponse(filteredPages, lemmasSortedAndFilter, query, offset, limit));
    }
    private List<Long> getUrlListBySiteUrl(String siteUrl) {
        List<Long> sitesId = new ArrayList<>();
        if (siteUrl == null) {
            siteRepository.findBySiteStatus(SiteStatus.INDEXED)
                    .forEach(site -> sitesId.add(site.getId()));
        } else {
            sitesId.add(siteRepository.findByUrl(siteUrl).get(0).getId());
        }
        return sitesId;
    }
    private SearchResponse buildResponse(List<Page> pages, List<Lemma> lemmasSortedAndFilter, String query, int offset, int limit) {
        data = new TreeSet<>((o1, o2) -> {
            int result = Float.compare(o1.getRelevance(), o2.getRelevance());
            if (result == 0) result = (o1.getSite() + o1.getUri())
                    .compareTo(o2.getSite() + o2.getUri());
            return result;
            });

            pages.forEach(page -> {
                Data pageData = new Data();
                pageData.setSite(page.getSite().getUrl());
                pageData.setSiteName(page.getSite().getName());
                pageData.setUri(getUri(page));
                Document doc = Jsoup.parse(page.getContent());
                for (String field : Arrays.asList("title", "body")) {
                    Element element = doc.getElementsByTag(field).first();
                    if (field.equals("title") && element != null) pageData.setTitle(element.text());
                    else pageData.setTitle("...");
                    if (field.equals("body"))
                        if (element != null) {
                            pageData.setSnippet(lemmaService.getFragmentText(element.text(), query));
                        }
                }
                pageData.setRelevance(pageService.getRelativeRelevance(page, pages, lemmasSortedAndFilter));
                data.add(pageData);
            });
        List<Data> dataList = new LinkedList<>(data);
        if (dataList.size() > limit) dataList.subList(offset, limit);
        Collections.reverse(dataList);
        log.info("Найдены страницы: ");
        pages.forEach(page -> log.info("Ид страницы - " + page.getId()));
    return SearchResponse.builder()
                        .result(true)
                        .count(pages.size())
                        .data(dataList).build();
    }
    private String getUri(Page page) {
        if (page.getPath().indexOf("www") == -1)
            return page.getPath().substring(page.getSite().getUrl().length());
        return page.getPath().substring(page.getSite().getUrl().length() + 3);
    }
}
