package searchengine.dto.searcherUrls;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveAction;

@Getter
public class SearcherUrls extends RecursiveAction {
    private static Set<String> pathHtmlFiles;
    private Node node;

    public SearcherUrls(Node node, Set<String> pathHtmlFiles) {
        this.node = node;
        SearcherUrls.pathHtmlFiles = pathHtmlFiles;
    }


    @Override
    protected void compute() {
        String url = node.getUrl();
        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .ignoreHttpErrors(true)
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Elements elements = doc.select("a[href]");
        Set<String> urls = new HashSet<>();
        addUrl(elements, urls);
        if (urls.isEmpty()){
            return;
        }
        List<SearcherUrls> subTasks = new LinkedList<>();
        for (String urlString : urls) {
            Node child = new Node(urlString);
            SearcherUrls task = new SearcherUrls(child, pathHtmlFiles);
            task.fork();
            subTasks.add(task);
            node.addUrl(child);
        }
        subTasks.forEach(t->t.join());
    }
    private void addUrl(Elements elements, Set<String> urls){
        String domain = elements.attr("abs:href");
        for (Element element : elements){
            String childUrl = element.attr("abs:href");
            boolean check = checkPageOrUrl(childUrl);
            if (childUrl.contains(domain) && !pathHtmlFiles.contains(childUrl) && check){
                urls.add(childUrl);
                pathHtmlFiles.add(childUrl);
            }
        }
    }
    private boolean checkPageOrUrl(String url) {
        boolean urlBoolean = url.lastIndexOf("/") == url.length() - 1;
        int endIndex = url.lastIndexOf(".html");
        boolean page = url.lastIndexOf(".html") == url.length() - 5;
        return (urlBoolean || page);
    }
}

