package searchengine.dto.searcherUrls;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "connection-settings")
public class MyHTTPConnection {
    private String userAgent;
    private String referrer;
    public Connection getConnection(String url) {
        Connection connection = Jsoup.connect(url).ignoreHttpErrors(true)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-us; rvl.8.1.6) " +
                        "Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com").followRedirects(false);
        return connection;
    }
}
