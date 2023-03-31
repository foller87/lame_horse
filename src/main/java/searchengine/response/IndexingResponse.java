package searchengine.response;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import searchengine.services.IndexingService;
import searchengine.services.PageService;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IndexingResponse {
    private final IndexingService indexingService;
    private final PageService pageService;

    public ResponseEntity startIndexing() {
        Map<String, Object> response = new HashMap<>();
        boolean result = !indexingService.startIndexing();
        response.put("result", result);
        if (!result) response.put("error", "Индексация уже запущена");
        return ResponseEntity.ok(response);
    }
    public ResponseEntity stopIndexing(){
        Map<String, Object> response = new HashMap<>();
        boolean result = indexingService.stopIndexing();
        response.put("result", result);
        if (!result) response.put("error", "Индексация не запущена");
        return ResponseEntity.badRequest().body(response);
    }
    public ResponseEntity indexPage(String url){
        return ResponseEntity.ok(pageService.indexPage(url));
    }
}
