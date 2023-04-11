package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import searchengine.dto.searcherUrls.MyHTTPConnection;
import searchengine.dto.searcherUrls.Node;
import searchengine.dto.searcherUrls.SearcherUrls;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageServiceImpl implements PageService{
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final LemmaService lemmaService;
    private final MyHTTPConnection myHTTPConnection;

    @Override
    public void findUrlsOnSite(Site site, boolean flag) {
        if(site.getSiteStatus().equals(SiteStatus.FAILED)) return;

        log.info("Начало поиска ссылок " + LocalTime.now());

        String url = site.getUrl();
        Node node = new Node(url);
        Map<String, Integer> pathHtmlFiles = new ConcurrentHashMap<>();
        String nameSite = getNameSite(url);

        SearcherUrls searcher = new SearcherUrls(node, pathHtmlFiles, nameSite, flag);
        ForkJoinPool fjp = new ForkJoinPool(6);
        fjp.invoke(searcher);

        List<Page> pages = getListPages(site, pathHtmlFiles, flag);
        log.info("Ссылки получены с сайта "+ site.getUrl() + " время " + LocalTime.now());

        pages = pageRepository.saveAllAndFlush(pages);
        pages.forEach(page -> lemmaService.saveLemmasByPageAndSite(site, page, flag));
        log.info("Ссылки сохранены с сайта "+ site.getUrl() + " время " + LocalTime.now());
    }
    private Page setPageData(Site site, String url, int statusCode, boolean flag) {
//        myHTTPConnection = new MyHTTPConnection();
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
        //TODO: пока что url, далее необходимо заменить на path без домена
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
//        myHTTPConnection = new MyHTTPConnection();
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
}
