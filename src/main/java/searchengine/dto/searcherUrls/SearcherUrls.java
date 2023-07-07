package searchengine.dto.searcherUrls;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Data
@Slf4j
public class SearcherUrls extends RecursiveAction {
    private Map<String, Integer> pathHtmlFiles;
    private Node node;
    private String domain;
    @Autowired
    private MyHTTPConnection myHTTPConnection;
    private boolean flag;

    public SearcherUrls(Node node, Map<String, Integer> pathHtmlFiles, String domain, boolean flag,
                        MyHTTPConnection myHTTPConnection) {
        this.node = node;
        this.pathHtmlFiles = pathHtmlFiles;
        this.domain = domain;
        this.flag = flag;
        this.myHTTPConnection = myHTTPConnection;
    }

    @Override
    protected void compute() {
        String url = node.getUrl();
        Connection connection = myHTTPConnection.getConnection(url);
        Document doc;
        if (!flag) {
            try {
                Thread.sleep(1000);
                doc = connection.get();
                Thread.sleep(6000);
            } catch (IOException | InterruptedException e) {
                log.info("Присвоен статус 404. Ошибка на странице " + url + " " + e.getMessage());
                pathHtmlFiles.put(url, 404);
                return;
            }
            Elements elements = doc.select("a[href]");
            Set<String> urls = new HashSet<>();
            addUrl(elements, urls, connection);
            if (urls.isEmpty()) {
                return;
            }
            List<SearcherUrls> subTasks = new LinkedList<>();
            for (String urlString : urls) {
                Node child = new Node(urlString);
                SearcherUrls task = new SearcherUrls(child, pathHtmlFiles, domain, flag, myHTTPConnection);
                task.fork();
                subTasks.add(task);
                node.addUrl(child);
            }
            subTasks.forEach(ForkJoinTask::join);
        }
    }
    private void addUrl(Elements elements, Set<String> urls, Connection connection){
        for (Element element : elements){
            String childUrl = element.attr("abs:href");
            boolean check = checkPageOrUrl(childUrl);
            int beginIndex = childUrl.indexOf(domain) + domain.length(); //для www.
            String urlDomain = childUrl.substring(0, beginIndex);
                if (childUrl.contains(domain) && !pathHtmlFiles.containsKey(childUrl) && check &&
                        urlDomain.length() <= domain.length() + 12) {
                    urls.add(childUrl);
                    int statusCode = getStatusCode(connection);
                    pathHtmlFiles.put(childUrl, statusCode);
                    log.info("Добавляем "+ childUrl + " Уже " + pathHtmlFiles.size() + " в коллекции");
                }
            }
        }
    private int getStatusCode(Connection connection) {
        int statusCode;
        try {
            Connection.Response response = connection.execute();
            statusCode = response.statusCode();
        } catch (IOException e) {
            statusCode = 404;
        }
        return statusCode;
    }
    private boolean checkPageOrUrl(String url) {
        boolean urlBoolean = url.lastIndexOf("/") == url.length() - 1;
        int endIndex = url.lastIndexOf(".html");
        boolean page = endIndex == url.length() - 5;
        return (urlBoolean || page);
    }
}

