package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.response.IndexingResponse;
import searchengine.services.IndexingService;
import searchengine.services.IndexingServiceIml;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingResponse indexingResponse;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {
        return indexingResponse.startIndexing();
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing(){
        return indexingResponse.stopIndexing();
    }
    @PostMapping("/indexPage/{url}")
    public ResponseEntity indexPage(@PathVariable String url){
        return indexingResponse.indexPage(url);
    }
}
