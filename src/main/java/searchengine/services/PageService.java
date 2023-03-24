package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.searcherUrls.MyHTTPConnection;
import searchengine.dto.searcherUrls.Node;
import searchengine.dto.searcherUrls.SearcherUrls;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class PageService {
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private SiteRepository siteRepository;
    private MyHTTPConnection myHTTPConnection;

    public void findUrlsOnSite(Site site) {
        long start = System.currentTimeMillis();
        System.out.println("Начало посика ссылок " + LocalTime.now());
        String url = site.getUrl();
        Node node = new Node(url);
        Map<String, Integer> pathHtmlFiles = new ConcurrentHashMap<>();
        String nameSite = getNameSite(url);
        SearcherUrls searcher = new SearcherUrls(node, pathHtmlFiles, nameSite);
        ForkJoinPool fjp = new ForkJoinPool(6);
        fjp.invoke(searcher);
        List<Page> pages = getListPages(site, pathHtmlFiles);
        System.out.println("Ссылки получены с сайта"+ site.getUrl() + " время " + LocalTime.now());
        long before = System.currentTimeMillis() - start;
        System.out.println("Времени прошло с сайта"+ site.getUrl() + " время "+ before);
        pageRepository.saveAllAndFlush(pages);
        System.out.println("Ссылки сохранены с сайта"+ site.getUrl() + " время " + LocalTime.now());
        System.out.println("Времени прошло с сайта"+ site.getUrl() + " время " + (System.currentTimeMillis() - before));
    }
    private Page getPage(Site site, String url, int statusCode) {
        Page newPage = new Page();
        myHTTPConnection = new MyHTTPConnection();
        newPage.setSite(site);
        newPage.setCode(statusCode);
        newPage.setPath(url);
        if (statusCode == 200 || statusCode == 301) {
            try {
                Connection connection = myHTTPConnection.getConnection(url);
                newPage.setContent(connection.timeout(6 * 1000).get().html());
            } catch (IOException e) {
                System.out.println("PageService на ссылке " + url + " " + newPage.getCode());
                throw new RuntimeException(e);
            }
        } else newPage.setContent("");
        return newPage;
    }
    private List<Page> getListPages(Site site, Map<String, Integer> pathHtmlFiles) {
        List<Page> pages = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : pathHtmlFiles.entrySet()) {
            pages.add(getPage(site, entry.getKey(), entry.getValue()));
        }
        if (pages.size() == 30) {
            pageRepository.saveAllAndFlush(pages);
            pages = new ArrayList<>();
        }
        return pages;
    }
    private String getNameSite(String url) {
        String name = url.substring(url.lastIndexOf("//") + 2, (url.length() - 1));
        return name;
    }
}
