package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.HashMap;
import java.util.List;

public interface LemmaService {
    HashMap<String, Integer> getLemmasFromHTML(String textHTML);
    void saveLemmasByPageAndSite(Site site, Page page, boolean flag);
    List<Lemma> getLemmasByQuery(String query);
    String getFragmentText(String contentText, String query);
}
