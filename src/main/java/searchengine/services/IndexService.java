package searchengine.services;

import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

public interface IndexService {
    Index saveIndexInRepository (Page page, Lemma lemma, int rankLemma);
    List<Index> findIndexByPageAndLemma(Page page, Lemma lemma);
    void saveAllInAndFlush(List<Index> indexes);
}
