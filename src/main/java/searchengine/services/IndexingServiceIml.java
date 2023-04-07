package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
@EnableAspectJAutoProxy
public class IndexingServiceIml implements IndexingService{
    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private volatile boolean flag;


    @Override
    public ResponseEntity startIndexing() {
        flag = false;
        Map<String, Object> response = new HashMap<>();
        boolean isIndexing = siteService.checkIndexingSite();
        response.put("result", !isIndexing);
        if (isIndexing) {
            response.put("error", "Индексация уже запущена");
            return ResponseEntity.ok(response);
        }
        Set<Site> sites = siteService.saveSitesDB();
        List<Thread> finderThread = new ArrayList<>();
        for (Site site : sites) {
                finderThread.add(new Thread(() ->
                        pageService.findUrlsOnSite(site, flag)));
        }
        finderThread.forEach(Thread::start);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity stopIndexing() {
        Map<String, Object> response = new HashMap<>();
        boolean checkSiteStatus = siteService.checkIndexingSite();
        response.put("result", checkSiteStatus);
        if (!checkSiteStatus) flag = true;
        else response.put("error", "Индексация не запущена");
        return ResponseEntity.ok(response);
    }
    @Override
    public ResponseEntity pageIndexing(String url) {
        Map<String, Object> response = new HashMap<>();
        String domain = getDomain(url);
        if (domain.isBlank()) {
            response.put("result", false);
            response.put("error", "Адрес страницы указан неверно.");
            return ResponseEntity.ok(response);
        }
        List<Site> siteList = siteService.findSiteByUrl(domain);
        if (siteList.isEmpty()) {
            response.put("result", false);
            response.put("error", "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");
            return ResponseEntity.ok(response);
        }
        Site site = siteList.get(0);
        Page page = pageService.getPageByUrl(site, url);
        lemmaService.saveLemmasByPageAndSite(site, page, flag);
        response.put("result", true);
        return ResponseEntity.ok(response);
    }
    private String getDomain(String url) {
        String regex = "http[?s]://[^/]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return url.substring(matcher.start(), matcher.end() + 1)
                    .replaceAll("www\\.", "");
        }
        else url = "";
        return url;
    }
}
