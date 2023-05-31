package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

public interface PageService {
    void findUrlsOnSite(Site site, boolean flag);
    Page getPageByUrl(Site site, String url);
    List<Page> getPagesByLemmas(List<Lemma> lemmas);
    List<Page> filterPages(List<Page> mainListPage, List<Page> receivedListPage);
    float getRelativeRelevance(Page page, List<Page> pageList, List<Lemma> queryLemmas);
}
