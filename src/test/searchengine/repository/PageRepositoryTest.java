package searchengine.repository;

import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import searchengine.services.PageServiceImpl;

@SpringBootTest
class PageRepositoryTest {
    @Mock
    private PageRepository pageRepository;
    @MockBean
    private PageServiceImpl pageService;


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