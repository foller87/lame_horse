package searchengine.dto.statistics;

import lombok.Data;
import searchengine.model.SiteStatus;

import java.time.LocalDateTime;

@Data
public class DetailedStatisticsItem {
    private String url;
    private String name;
    private SiteStatus status;
    private LocalDateTime statusTime;
    private String error;
    private long pages;
    private long lemmas;
}
