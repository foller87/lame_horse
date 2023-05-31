package searchengine.dto.searcherUrls;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class Node {
    private String url;
    private Set<Node> childrenUrls;
    public Node (String url){
        this.url = url;
        childrenUrls = new HashSet<>();
    }
    public void addUrl(Node child){
        childrenUrls.add(child);
    }
    @Override
    public boolean equals(Object obj) {
        return getUrl().equals(((Node) obj).getUrl());
    }
    @Override
    public int hashCode() {
        return getUrl().hashCode();
    }
}
