package searchengine.repository;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.PageServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class PageRepositoryTest {
    @Autowired
    private PageRepository pageRepository;
    @MockBean
    private PageServiceImpl pageService;

//
//    @Test
//    void findByPath(){
//        String path = "https://habr.com/ru/company/";
//        Page page = Mockito.mock(Page.class);
//        page.setPath(path);
//        List<Page> pages = new ArrayList<>();
//        pages.add(page);
//        when(pageRepository.findPageByPath(path)).thenReturn(pages);
//
//        List<Page> actual = pageRepository.findPageByPath(path);
//
//        assertEquals(pages, actual);
//    }
//    @Test
//    void findByLemmaList(){
//        Site site = Site.builder()
//                .name("Svetlovka.ru")
//                .url("https://svetlovka.ru/")
//                .id(257L)
//                .build();
//        Page page1 = Page.builder()
//                .path("1234")
//                .site(site)
//                .build();
//        Page page2 = Page.builder()
//                .path("4321")
//                .site(site)
//                .build();
//        Lemma lemma = Lemma.builder()
//                .lemma("страхов")
//                .frequency(3)
//                .site(site).build();
//        Lemma lemma2 = Lemma.builder()
//                .lemma("страхов")
//                .frequency(3)
//                .site(site).build();
//        List<Page> pages = new ArrayList<>();
//        pages.add(page1);
//        pages.add(page2);
//
//        List<Lemma> lemmas = new ArrayList<>();
//        lemmas.add(lemma);
//        lemmas.add(lemma2);
//
//        List<Page> actual = pageRepository.findByLemmaList(lemmas);
//
//        assertEquals(pages, actual);
//    }
}