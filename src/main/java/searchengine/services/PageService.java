package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.lemmatizer.Lemmatizer;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PageService {
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private SiteRepository siteRepository;
    private final Lemmatizer lemmatizer;
    private MyHTTPConnection myHTTPConnection;

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
        String name = url.substring(url.lastIndexOf("//") + 2, (url.length() - 1));
        return name;
    }
    public Map<String, Object> indexPage(String url) {
        Map<String, Object> response = new HashMap<>();
        String domain = getDomain(url);
        if (domain.isBlank()) {
            response.put("result", false);
            response.put("error", "Адрес страницы указан неверно.");
            return response;
        }
        List<Site> site = siteRepository.findByUrl(domain);
        if (site.isEmpty()) {
            response.put("result", false);
            response.put("error", "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");
            return response;
        }
        //TODO: пока что url, далее необходимо заменить на path без домена
        pageRepository.delete(pageRepository.findPageByPath(url));
        Page page = Page.builder().path(url).site(site.get(0)).build();
        try {
            Connection connection = myHTTPConnection.getConnection(url);
            page.setContent(connection.timeout(6 * 1000).get().html());
            page.setCode(connection.execute().statusCode());
        } catch (IOException e) {
            System.out.println("PageService на ссылке " + url + " " + page.getCode());
            page.setCode(404);
            page.setContent("");
        }
        Map<String, Integer> lemmas = lemmatizer.getLemmas(page.getContent());
        pageRepository.save(page);
        response.put("result", true);
        return response;
    }
    public String getDomain(String url) {
        String regex = "http[?s]://[^/]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) return url.substring(matcher.start(), matcher.end() + 1);
        else url = "";
        return url;
    }
}
