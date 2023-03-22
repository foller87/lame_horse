package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {
        Map<String, Object> response = new HashMap<>();
        boolean result = !indexingService.startIndexing();
        response.put("result", result);
        if (result)
        {
            return ResponseEntity.ok(response);
        }
        response.put("error", "Индексация уже запущена");
        return ResponseEntity.ok(response);
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing(){
        Map<String, Object> response = new HashMap<>();
        boolean result = indexingService.stopIndexing();
        response.put("result", result);
        if (!result) response.put("error", "Индексация не запущена");
        return ResponseEntity.ok(response);
    }
}
