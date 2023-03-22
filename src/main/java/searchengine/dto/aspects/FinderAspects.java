package searchengine.dto.aspects;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.SiteRepository;
import searchengine.services.SiteService;

import java.util.Set;

@Component
@Aspect
public class FinderAspects {
    @Autowired
    private SiteService siteService;
    @Autowired
    private SiteRepository repository;

    @Pointcut("execution( * searchengine.services.PageService.findUrlsOnSite(..))")
    private void changeStatusSite(){}

    @AfterReturning("changeStatusSite()")
    private void afterReturningFindUrlsOnSiteAdvice(){
        Set<Site> sites = siteService.getSitesByStatus(SiteStatus.INDEXING);
        siteService.changeSiteStatus(sites, SiteStatus.INDEXED);
    }
    @AfterThrowing("changeStatusSite()")
    private void afterThrowingFindUrlsOnSiteAdvice(){
        Set<Site> sites = siteService.getSitesByStatus(SiteStatus.INDEXING);
        siteService.changeSiteStatus(sites, SiteStatus.FAILED);
    }
}
