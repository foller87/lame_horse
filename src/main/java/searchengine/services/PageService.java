package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.springframework.stereotype.Service;
import searchengine.dto.searcherUrls.MyHTTPConnection;
import searchengine.dto.searcherUrls.Node;
import searchengine.dto.searcherUrls.SearcherUrls;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
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
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaService lemmaService;
    private MyHTTPConnection myHTTPConnection;
    private final IndexRepository indexRepository;

    public void findUrlsOnSite(Site site) {
        System.out.println("Начало поиска ссылок " + LocalTime.now());
        String url = site.getUrl();
        Node node = new Node(url);
        Map<String, Integer> pathHtmlFiles = new ConcurrentHashMap<>();
        String nameSite = getNameSite(url);
        SearcherUrls searcher = new SearcherUrls(node, pathHtmlFiles, nameSite);
        ForkJoinPool fjp = new ForkJoinPool(6);
        fjp.invoke(searcher);
        List<Page> pages = getListPages(site, pathHtmlFiles);
        System.out.println("Ссылки получены с сайта"+ site.getUrl() + " время " + LocalTime.now());
        pageRepository.saveAllAndFlush(pages);
        System.out.println("Ссылки сохранены с сайта"+ site.getUrl() + " время " + LocalTime.now());
    }
    private Page setPageData(Site site, String url, int statusCode) {
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
                newPage.setCode(404);
                newPage.setContent("");
            }
        } else newPage.setContent("");
        return newPage;
    }
    private List<Page> getListPages(Site site, Map<String, Integer> pathHtmlFiles) {
        List<Page> pages = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : pathHtmlFiles.entrySet()) {
            pages.add(setPageData(site, entry.getKey(), entry.getValue()));
        }
        if (pages.size() == 30) {
            pageRepository.saveAll(pages);
            pages = new ArrayList<>();
        }
        return pages;
    }
    private String getNameSite(String url) {
        return url.substring(url.lastIndexOf("//") + 2, (url.length() - 1));
    }
    public Page getPageByUrl(Site site, String url) {
        //TODO: пока что url, далее необходимо заменить на path без домена
        List<Page> pageList = pageRepository.findPageByPath(url);
        if(!pageList.isEmpty())pageRepository.delete(pageList.get(0));
        Page page = Page.builder().path(url).site(site).build();
        myHTTPConnection = new MyHTTPConnection();
        try {
            Connection connection = myHTTPConnection.getConnection(url);
            page.setContent(connection.timeout(6 * 1000).get().html());
            page.setCode(connection.execute().statusCode());
        } catch (IOException e) {
            System.out.println("PageService на ссылке " + url + " " + page.getCode());
            page.setCode(404);
            page.setContent("");
        }
        page = pageRepository.save(page);
        return page;
    }
}
