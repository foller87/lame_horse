package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
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
    private final IndexService indexService;
    private List<Thread> finderThread;


    @Override
    public boolean startIndexing() {
        boolean isIndexing = siteService.checkIndexingSite();
        if (isIndexing) return true;
        Set<Site> sites = siteService.saveSitesDB();
        finderThread = new ArrayList<>();
        for (Site site : sites) {
            finderThread.add(new Thread(() ->
                    pageService.findUrlsOnSite(site)));
        }
        finderThread.forEach(Thread::start);
        return false;
    }

    @Override
    public boolean stopIndexing() {
        boolean checkSiteStatus = siteService.checkIndexingSite();
        if (!checkSiteStatus) finderThread.forEach(Thread::interrupt);
        return checkSiteStatus;
    }
    @Override
    public Map<String, Object> pageIndexing(String url) {
        Map<String, Object> response = new HashMap<>();
        String domain = getDomain(url);
        if (domain.isBlank()) {
            response.put("result", false);
            response.put("error", "Адрес страницы указан неверно.");
            return response;
        }
        List<Site> siteList = siteService.findSiteByUrl(domain);
        if (siteList.isEmpty()) {
            response.put("result", false);
            response.put("error", "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");
            return response;
        }
        Site site = siteList.get(0);
        Page page = pageService.getPageByUrl(site, url);
        HashMap<String, Integer> lemmas = lemmaService.getLemmasFromHTML(page.getContent());
        for(Map.Entry<String, Integer> entry : lemmas.entrySet()){
            Lemma lemma = lemmaService.saveLemma(site, entry.getKey());
            indexService.saveIndexInRepository(page, lemma, entry.getValue());
        };
        response.put("result", true);
        return response;
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
