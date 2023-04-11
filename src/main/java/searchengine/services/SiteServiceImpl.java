package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import searchengine.config.SiteModel;
import searchengine.config.SitesList;
import searchengine.dto.searcherUrls.MyHTTPConnection;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Component
@Slf4j
public class SiteServiceImpl implements SiteService
{
    private final SiteRepository siteRepository;
    private final SitesList sitesList;
    private final LemmaRepository lemmaRepository;
    private final MyHTTPConnection myHTTPConnection;

    @Override
    public Set<Site> saveSitesDB()
    {
        Set<Site> sites = new HashSet<>();
        for (SiteModel siteModel : sitesList.getSites()) {
            sites.add(addNewSites(siteModel));
        }
        return sites;
    }
    private Site addNewSites(SiteModel siteModel) {
        Site newSite = new Site();
        String urlSiteToConfig = removePrefixAndAddSuffix(siteModel.getUrl());
        List<Site> sites = siteRepository.findByUrl(urlSiteToConfig);
        if (!sites.isEmpty()) sites.forEach(s-> {
            lemmaRepository.deleteAll();
            siteRepository.deleteById(s.getId());
        });
        newSite.setUrl(urlSiteToConfig);
        newSite.setName(siteModel.getName());
        newSite.setSiteStatus(getStatusSite(newSite));
        if (newSite.getSiteStatus().equals(SiteStatus.FAILED))
            newSite.setLastError("Ошибка индексации: Главная страница сайта не доступна");
        newSite.setStatusTime(LocalDateTime.now());
        newSite.setId(siteRepository.save(newSite).getId());
        return newSite;
    }
    private SiteStatus getStatusSite(Site site){
        int statusCode = checkingConnection(site);
        return PageServiceImpl.checkingStatusOfThePageCode(statusCode) ? SiteStatus.FAILED : SiteStatus.INDEXING;
    }
    private String removePrefixAndAddSuffix(String url) {
        String newUrl = url.replaceAll("www\\.", "");
        if(url.lastIndexOf("/") < url.length() - 1) {
            newUrl += "/";
        }
        return newUrl;
    }
    @Override
    public boolean checkIndexingSite(){
        boolean check = false;
        Iterable<Site> sites = siteRepository.findAll();
        for (Site site : sites) {
            if (site.getSiteStatus().equals(SiteStatus.INDEXING)) {
                check = true;
                break;
            }
        }
        return check;
    }
    @Override
    public List<Site> findSiteByUrl(String url){
        return siteRepository.findByUrl(url);
    }
    private int checkingConnection(Site site) {
        String url = site.getUrl();
        int statusCode = 0;
        try {
            statusCode = myHTTPConnection.getConnection(url).execute().statusCode();
        } catch (IOException e) {
            log.error("Сайт не доступен " + url + " " + statusCode);
            statusCode = 404;
        }
        return statusCode;
    }
}
