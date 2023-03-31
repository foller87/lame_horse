package searchengine.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PageServiceTest {
    @Autowired
    private PageService pageService;
    @MockBean
    private PageRepository pageRepository;
    @MockBean
    private SiteRepository siteRepository;

    @Test
    void getDomain(){
        String url = "https://playback.ru/product/109210.html";
        String actual = pageService.getDomain(url);

        assertEquals("https://playback.ru/", actual);
    }

}