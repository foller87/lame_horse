package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

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
    @GetMapping("/search")
    public ResponseEntity search(@RequestParam(required = false) String query,
                                 @RequestParam(required = false) String site,
                                 @RequestParam(required = false) Integer offset,
                                 @RequestParam(required = false) Integer limit) {
        return searchService.search(query, site, offset, limit);
    }
}
