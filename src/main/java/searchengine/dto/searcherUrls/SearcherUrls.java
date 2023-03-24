package searchengine.dto.searcherUrls;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveAction;

@Data
@NoArgsConstructor
public class SearcherUrls extends RecursiveAction {
    private Map<String, Integer> pathHtmlFiles;
    private Node node;
    private String domain;
    private MyHTTPConnection myHTTPConnection;

    public SearcherUrls(Node node, Map<String, Integer> pathHtmlFiles, String domain) {
        this.node = node;
        this.pathHtmlFiles = pathHtmlFiles;
        this.domain = domain;
        myHTTPConnection = new MyHTTPConnection();
    }

    @Override
    protected void compute() {
        String url = node.getUrl();
        Connection connection = myHTTPConnection.getConnection(url);
        Document doc;
        int statusCode = getStatusCode(url, connection);
        if (statusCode == 200 || statusCode == 301) {
            try {
                doc = connection.get();
            } catch (IOException e) {
                System.out.println("SearcherUrls на ссылке " + url);
                throw new RuntimeException(e);
            }
            Elements elements = doc.select("a[href]");
            Set<String> urls = new HashSet<>();
            addUrl(elements, urls);
            if (urls.isEmpty()) {
                return;
            }
            List<SearcherUrls> subTasks = new LinkedList<>();
            for (String urlString : urls) {
                Node child = new Node(urlString);
                SearcherUrls task = new SearcherUrls(child, pathHtmlFiles, domain);
                task.fork();
                subTasks.add(task);
                node.addUrl(child);
            }
            subTasks.forEach(t -> t.join());
        }
    }
    private void addUrl(Elements elements, Set<String> urls){
        for (Element element : elements){
            String childUrl = element.attr("abs:href");
            boolean check = checkPageOrUrl(childUrl);
            if (childUrl.contains(domain) && !pathHtmlFiles.containsKey(childUrl) && check){
                urls.add(childUrl);
            }
        }
    }
    private int getStatusCode(String url, Connection connection) {
        int statusCode;
        try {
            Connection.Response response = connection.execute();
            statusCode = response.statusCode();
            pathHtmlFiles.put(url, statusCode);
        } catch (IOException e) {
        System.out.println("SearcherUrls на ссылке " + url);
        throw new RuntimeException(e);
        }
        return statusCode;
    }
    private String checkSiteUrl(String url) {
        String newUrl = url.replaceAll("www\\.", "");
        if(url.lastIndexOf("/") < url.length() - 1) {
            newUrl += "/";
        }
        return newUrl;
    }
    private boolean checkPageOrUrl(String url) {
        boolean urlBoolean = url.lastIndexOf("/") == url.length() - 1;
        int endIndex = url.lastIndexOf(".html");
        boolean page = endIndex == url.length() - 5;
        return (urlBoolean || page);
    }
}

