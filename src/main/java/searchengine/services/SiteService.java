package searchengine.services;

import searchengine.model.Site;

import java.util.List;
import java.util.Set;

public interface SiteService {
    Set<Site> saveSitesDB();
    boolean checkIndexingSite();
    List<Site> findSiteByUrl(String url);
}
