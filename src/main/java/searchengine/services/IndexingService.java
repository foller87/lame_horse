package searchengine.services;

import org.springframework.http.ResponseEntity;

public interface IndexingService {
    ResponseEntity startIndexing();
    ResponseEntity stopIndexing();
    ResponseEntity pageIndexing(String url);
}
