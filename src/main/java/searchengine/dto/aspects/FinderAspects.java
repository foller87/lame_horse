package searchengine.dto.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.SiteService;

import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j
public class FinderAspects {
    @Autowired
    private SiteService siteService;

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository repository;

    @Pointcut("execution( * searchengine.services.PageService.findUrlsOnSite(..))")
    private void changeStatusSite(){}

    @AfterReturning(value = "changeStatusSite()")
    private void afterReturningFindUrlsOnSiteAdvice(JoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();
        for (Object obj : args) {
            if (obj instanceof Site site) {
                if (site.getSiteStatus().equals(SiteStatus.FAILED)) return;
                site.setSiteStatus(SiteStatus.INDEXED);
                log.info("Успешная индексация " + LocalDateTime.now() + " сайта " + site.getName());
                repository.saveAndFlush(site);
            }
        }
    }
    @AfterThrowing(value = "changeStatusSite()", throwing = "exeption")
    private void afterThrowingFindUrlsOnSiteAdvice(JoinPoint joinPoint, Throwable exeption){
        Object[] args = joinPoint.getArgs();
        for (Object obj : args) {
            if (obj instanceof Site site) {
                site.setSiteStatus(SiteStatus.FAILED);
                site.setLastError("Ошибка индексации: " + exeption.getMessage());
                log.info("Фейл " + LocalDateTime.now() + " " + site.getName());
                repository.saveAndFlush(site);
            }
        }

    }
}
