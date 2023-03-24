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

import java.time.LocalDateTime;
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
        Set<Site> sites = repository.findBySiteStatus(SiteStatus.INDEXING);
        sites.forEach(s->s.setSiteStatus(SiteStatus.INDEXED));
        repository.saveAllAndFlush(sites);
        System.out.println("Успешная индексация " + LocalDateTime.now());
    }
    @AfterThrowing("changeStatusSite()")
    private void afterThrowingFindUrlsOnSiteAdvice(){
        Set<Site> sites = repository.findBySiteStatus(SiteStatus.INDEXING);
        sites.forEach(s->s.setSiteStatus(SiteStatus.FAILED));
        repository.saveAllAndFlush(sites);
        System.out.println("Какой то фейл " + LocalDateTime.now());
    }
}
