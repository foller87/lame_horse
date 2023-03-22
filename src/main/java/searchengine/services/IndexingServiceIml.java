package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@EnableAspectJAutoProxy
public class IndexingServiceIml implements IndexingService{
    @Autowired
    private final SiteService siteService;
    @Autowired
    private PageService pageService;
//    volatile boolean flag;

    @Override
    public boolean startIndexing() {
        boolean isIndexing = siteService.checkIndexingSite();
        if (isIndexing) return true;
        Set<Site> sites = siteService.saveSitesDB();
//        flag = false;
        List<Thread> finderThread = new ArrayList<>();
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Site site : sites) {
            finderThread.add(new Thread(() -> pageService.findUrlsOnSite(site)));
//            executorService.execute(() -> pageService.findUrlsOnSite(site));
        }
        finderThread.forEach(Thread::start);
        return false;
    }

    @Override
    public boolean stopIndexing() {
        boolean checkSiteStatus = siteService.checkIndexingSite();
        if (checkSiteStatus) {
//            flag = true;
//            Set<Site> sites = siteService.getSitesByStatus(SiteStatus.INDEXING);
//            siteService.changeSiteStatus(sites, SiteStatus.FAILED);
            Thread.interrupted();
        }
        return checkSiteStatus;
    }
}
