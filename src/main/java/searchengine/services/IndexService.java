package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;

public interface IndexService {
    void saveIndexInRepository (Page page, Lemma lemma, int rankLemma);
}
