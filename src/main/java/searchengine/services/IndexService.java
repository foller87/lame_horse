package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class IndexService {
    private final IndexRepository indexRepository;

    public Index saveIndexInRepository(Page page, Lemma lemma, int rankLemma){
        Index index = Index.builder().pageSites(page).lemma(lemma).rank(rankLemma).build();
        return indexRepository.save(index);
    }
}
