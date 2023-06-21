package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.stereotype.Service;
import searchengine.dto.searcherUrls.MyHTTPConnection;
import searchengine.dto.searcherUrls.Node;
import searchengine.dto.searcherUrls.SearcherUrls;
import searchengine.model.*;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageServiceImpl implements PageService{
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final MyHTTPConnection myHTTPConnection;

    @Override
    public void findUrlsOnSite(Site site, boolean flag) {
        if(site.getSiteStatus().equals(SiteStatus.FAILED)) return;

        log.info("Начало поиска ссылок " + LocalTime.now());

        String url = site.getUrl();
        Node node = new Node(url);
        Map<String, Integer> pathHtmlFiles = new ConcurrentHashMap<>();
        String nameSite = getNameSite(url);

        SearcherUrls searcher = new SearcherUrls(node, pathHtmlFiles, nameSite, flag, myHTTPConnection);
        ForkJoinPool fjp = new ForkJoinPool(12);
        fjp.invoke(searcher);

        List<Page> pages = getListPages(site, pathHtmlFiles, flag);
        log.info("Получено " + pages.size() + " ссылок с сайта "+ site.getUrl() + " время " + LocalTime.now());

        pages.forEach(pageRepository::save);
        pages.forEach(page -> lemmaService.saveLemmasByPageAndSite(site, page, flag));
        log.info("Ссылки сохранены с сайта "+ site.getUrl() + " время " + LocalTime.now());
    }
    private Page setPageData(Site site, String url, int statusCode, boolean flag) {
        Page newPage = Page.builder().site(site).code(statusCode).path(url).build();
        if (!checkingStatusOfThePageCode(statusCode) && !flag) {
            setStatusCodeAndContent(newPage);
        } else newPage.setContent("");
        return newPage;
    }
    private List<Page> getListPages(Site site, Map<String, Integer> pathHtmlFiles, boolean flag) {
        List<Page> pages = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : pathHtmlFiles.entrySet()) {
            pages.add(setPageData(site, entry.getKey(), entry.getValue(), flag));
        }
        if (pages.size() == 30) {
            pageRepository.saveAllAndFlush(pages);
            pages = new ArrayList<>();
        }
        return pages;
    }
    private String getNameSite(String url) {
        return url.substring(url.lastIndexOf("//") + 2, (url.length() - 1));
    }
    @Override
    public Page getPageByUrl(Site site, String url) {
        List<Page> pageList = pageRepository.findPageByPath(url);
        if(!pageList.isEmpty()) {
            lemmaRepository.updateLemmaByPageIdNative(pageList.get(0).getId());
            pageRepository.delete(pageList.get(0));
        }
        Page page = Page.builder().path(url).site(site).build();
        setStatusCodeAndContent(page);
        page = pageRepository.save(page);
        return page;
    }
    private void setStatusCodeAndContent(Page page) {
        String url = page.getPath();
        try {
            Connection connection = myHTTPConnection.getConnection(url);
            page.setContent(connection.timeout(6 * 1000).get().html());
            page.setCode(connection.execute().statusCode());
        } catch (IOException e) {
            log.error("Страница по адресу " + url + " не доступна " + page.getCode());
            page.setCode(404);
            page.setContent("");
        }
    }
    public static boolean checkingStatusOfThePageCode(int statusCode){
        String check = String.valueOf(statusCode);
        return (check.indexOf("5") == 0 || check.indexOf("4") == 0);
    }
    @Override
    public List<Page> getPagesByLemmas(List<Lemma> lemmas){
        List<Page> pages = new ArrayList<>();
        for(int i = 0; i < lemmas.size() - 1; i++) {
            Lemma lemma = lemmas.get(i);
            List<Page> pagesByLemma = pageRepository.findByLemmaId(lemma.getId());
            if (pages.isEmpty() && !pagesByLemma.isEmpty())pages.addAll(pagesByLemma);
            pages = filterPages(pages, pagesByLemma);
        }
        return pages;
    }
    @Override
    public List<Page> filterPages(List<Page> mainListPage, List<Page> receivedListPage){
        List<Page> pages = new ArrayList<>();
        mainListPage.forEach(page -> {
            if (receivedListPage.contains(page)) pages.add(page);
        });
        return pages;
    }
    private Map<Page, Map<Lemma, Float>> getRankLemmas(Page page, List<Lemma> queryLemmas) {
        Map<Page, Map<Lemma, Float>> rankLemmasOnPage = new HashMap<>();
        queryLemmas.forEach(lemma -> {
            List<Index> indexList = indexService.findIndexByPageAndLemma(page, lemma);
            if (!indexList.isEmpty()) {
                Map<Lemma, Float> rankLemmaOnPage = new LinkedHashMap<>();
                rankLemmaOnPage.put(lemma, indexList.get(0).getRank());
                rankLemmasOnPage.put(page, rankLemmaOnPage);
            }
        });
        return rankLemmasOnPage;
    }
    private Set<Float> getRanksSet(Map<Page, Map<Lemma, Float>> rankLemmasOnPage){
        Set<Float> ranks = new TreeSet<>();
        rankLemmasOnPage.values().forEach(map ->
                ranks.addAll(map.values()));
        return ranks;
    }
    private float getMaxAbsoluteRelevance(List<Page> pageList, List<Lemma> queryLemmas) {
        Set<Float> ranks = new TreeSet<>();
        pageList.forEach(page -> {
            Map<Page, Map<Lemma, Float>> rankLemmas = getRankLemmas(page, queryLemmas);
            ranks.addAll(getRanksSet(rankLemmas));
        });
        return ranks.stream().max(Float::compareTo).get();
    }
    @Override
    public float getRelativeRelevance(Page page, List<Page> pageList, List<Lemma> queryLemmas) {
        Map<Page, Map<Lemma, Float>> rankLemmas = getRankLemmas(page, queryLemmas);
        Set<Float> ranks = new TreeSet<>(getRanksSet(rankLemmas));
        float absoluteRelevance = 0;
        for (Float rank : ranks) absoluteRelevance += rank;
        float maxAbsolutOnPages = getMaxAbsoluteRelevance(pageList, queryLemmas);
        return absoluteRelevance/maxAbsolutOnPages;
    }
}
