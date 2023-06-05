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
    public ResponseEntity search(@RequestParam(name = "query", required = false) String query,
                                 @RequestParam(name = "site", required = false) String site,
                                 @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                 @RequestParam(name = "limit",required = false, defaultValue = "20") int limit) {
        return searchService.search(query, site, offset, limit);
    }
}
