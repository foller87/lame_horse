package searchengine.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class IndexingServiceImlTest {
    @Autowired
    private IndexingService indexingService;

//    @Test
//    void getDomain(){
//        String url = "https://playback.ru/product/109210.html";
//        String actual = indexingService.getDomain(url);
//
//        assertEquals("https://playback.ru/", actual);
//    }
}