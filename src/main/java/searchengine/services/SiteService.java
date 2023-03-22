package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Component
public class SiteService
{
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private SitesList sitesList;

    public Set<Site> saveSitesDB()
    {
        Set<Site> sites = new HashSet<>();
        Iterable<Site> sitesDB = siteRepository.findAll();
        for (searchengine.config.Site site : sitesList.getSites()) {
            sites.add(addNewSites(site, sitesDB));
        }
        return sites;
    }
    private Site addNewSites(searchengine.config.Site site, Iterable<Site> sitesDB) {
        Site newSite = new Site();
        String urlSiteToConfig = checkSiteUrl(site.getUrl());
        for (Site s : sitesDB) {
            if (s.getUrl().equals(urlSiteToConfig)) {
                siteRepository.delete(s);
            }
        }
        newSite.setUrl(urlSiteToConfig);
        newSite.setName(site.getName());
        newSite.setSiteStatus(SiteStatus.INDEXING);
        newSite.setStatusTime(LocalDateTime.now());
        newSite.setId(siteRepository.save(newSite).getId());
        return newSite;
    }
    private String checkSiteUrl(String url) {
        String newUrl = url.replaceAll("www\\.", "");
        if(url.lastIndexOf("/") < url.length() - 1) {
            newUrl += "/";
        }
        return newUrl;
    }
    public boolean checkIndexingSite(){
        boolean check = false;
        Iterable<Site> sites = siteRepository.findAll();
        if(sites.iterator().hasNext()) {
            for (Site site : sites) {
                if (site.getSiteStatus().equals(SiteStatus.INDEXING)) {
                    check = true;
                    break;
                }
            }
        }
        return check;
    }
    public void changeSiteStatus(Set<Site> sites, SiteStatus status) {
        if(!sites.isEmpty()) {
            for (Site site : sites) {
                site.setSiteStatus(status);
                siteRepository.save(site);
            }
        }
    }
    public Set<Site> getSitesByStatus(SiteStatus status){
        Set<Site> sites = new HashSet<>();
        Iterable<Site> iterableSites = siteRepository.findAll();
        for (Site site : iterableSites) {
            if (site.getSiteStatus().equals(status)){
                sites.add(site);
            }
        }
        return sites;
    }
}
