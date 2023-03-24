package searchengine.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class SiteRepositoryTest {

    @Autowired
    private SiteRepository siteRepository;

    @Test
    void saveSite(){
        Site site = Site.builder().siteStatus(SiteStatus.INDEXED).statusTime(LocalDateTime.now())
                .url("https://lenta.ru/").lastError(null).build();
        siteRepository.save(site);
    }

    @Test
    void findByUrl() {
    }
}