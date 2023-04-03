package searchengine.services;

import java.util.Map;

public interface IndexingService {
    boolean startIndexing();
    boolean stopIndexing();
    Map<String, Object> pageIndexing(String url);
}
