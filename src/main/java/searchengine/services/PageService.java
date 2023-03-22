package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.searcherUrls.Node;
import searchengine.dto.searcherUrls.SearcherUrls;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class PageService {
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private SiteRepository siteRepository;

    public void findUrlsOnSite(Site site) {
        Set<String> urls = new HashSet<>();
        String regex = site.getUrl();

        Node node = new Node(regex);
        Set<String> pathHtmlFiles = new CopyOnWriteArraySet<>();
        SearcherUrls searcher = new SearcherUrls(node, pathHtmlFiles);
        ForkJoinPool fjp = new ForkJoinPool(2);
        fjp.invoke(searcher);
        pathHtmlFiles.forEach(p-> addNewPage(site, p));
    }
    private void addNewPage(Site site, String url) {
        Page newPage = new Page();
        newPage.setSite(site);
        newPage.setPath(url);
        try {
            Document document = Jsoup.connect(url)
                    .ignoreHttpErrors(true)
                    .get();
            newPage.setCode(document.connection().response().statusCode());
            if (newPage.getCode()!= 200) {
                newPage.setContent("");
            } else {
            String content = document.html();
            newPage.setContent(content);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pageRepository.save(newPage);
    }
}
