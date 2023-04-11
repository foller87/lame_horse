package searchengine.services;

import searchengine.model.Page;
import searchengine.model.Site;

public interface PageService {
    void findUrlsOnSite(Site site, boolean flag);
    Page getPageByUrl(Site site, String url);

}
