package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.model.Site;

public interface IndexingService {
    ResponseEntity startIndexing();
    ResponseEntity stopIndexing();
    ResponseEntity pageIndexing(String url);
    ResponseEntity search(String query, long offset, String site, long limit);
}
