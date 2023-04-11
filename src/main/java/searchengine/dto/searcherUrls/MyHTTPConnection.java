package searchengine.dto.searcherUrls;

import lombok.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "connection-settings")
public class MyHTTPConnection {
    @Value("user_agent")
    private String userAgent;
    @Value("referrer")
    private String referrer;
    public Connection getConnection(String url) {
        return Jsoup.connect(url).ignoreHttpErrors(true)
                .userAgent(userAgent)
                .referrer(referrer).followRedirects(false);
    }
}
