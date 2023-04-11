package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.HashMap;
import java.util.Map;

public interface LemmaService {
    HashMap<String, Integer> getLemmasFromHTML(String textHTML);
    void saveLemmasByPageAndSite(Site site, Page page, boolean flag);
}
