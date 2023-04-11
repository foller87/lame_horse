package searchengine.dto.statistics;

import lombok.Data;

@Data
public class SearchResponse {
    private boolean result;
    private long count;
    private SearchData searchData;
}
