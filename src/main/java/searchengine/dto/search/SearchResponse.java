package searchengine.dto.search;

import lombok.Builder;

import java.util.Set;

@lombok.Data
@Builder
public class SearchResponse {
    private boolean result;
    private long count;
    private Set<Data> data;
}
