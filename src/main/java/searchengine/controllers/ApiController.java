package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

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
        return indexingService.startIndexing();
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing(){
        return indexingService.stopIndexing();
    }
    @PostMapping("/indexPage")
    public ResponseEntity indexPage(String url){
        return indexingService.pageIndexing(url);
    }
}
