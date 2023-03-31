package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import searchengine.model.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@EnableAspectJAutoProxy
public class IndexingServiceIml implements IndexingService{
    @Autowired
    private final SiteService siteService;
    @Autowired
    private PageService pageService;
    private List<Thread> finderThread;


    @Override
    public boolean startIndexing() {
        boolean isIndexing = siteService.checkIndexingSite();
        if (isIndexing) return true;
        Set<Site> sites = siteService.saveSitesDB();
        finderThread = new ArrayList<>();
        for (Site site : sites) {
            finderThread.add(new Thread(() -> {
                while(!Thread.interrupted()){
                    pageService.findUrlsOnSite(site);
                }
            }));
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
//    TODO: 1. Проверить страницу в базе по домену.
//          2. Проверить существует ли страница.
//          3. Если существует удалить из таблиц page, lemma, index и проиндексировать.
//          4. Не существует, то проиндексировать.

    @Override
    public boolean indexPage(String url) {
        boolean check = true;
        return check;
    }
}
