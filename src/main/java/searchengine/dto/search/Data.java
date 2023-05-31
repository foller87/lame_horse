package searchengine.dto.search;

@lombok.Data
public class Data {
    private String site;
    private String siteName;
    private String title;
    private String uri;
    private String snippet;
    private float relevance;
}
