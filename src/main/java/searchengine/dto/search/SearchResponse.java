package searchengine.dto.search;

import lombok.Builder;

import java.util.List;

@lombok.Data
@Builder
public class SearchResponse {
    private boolean result;
    private long count;
    private List<Data> data;
}
