package searchengine.repository;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import searchengine.model.Page;
import searchengine.services.PageService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class PageRepositoryTest {
    @Mock
    private PageRepository pageRepository;
    @MockBean
    private PageService pageService;


//    @Test
//    void findByPath(){
//        String path = "https://habr.com/ru/company/";
//        Page page = Mockito.mock(Page.class);
//        page.setPath(path);
//        when(pageRepository.findPageByPath(path)).thenReturn(page);
//
//        Page actual = pageRepository.findPageByPath(path);
//
//        assertEquals(page, actual);
//    }
}