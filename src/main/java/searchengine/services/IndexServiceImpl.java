package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService{
    private final IndexRepository indexRepository;

    @Override
    public void saveIndexInRepository(Page page, Lemma lemma, int rankLemma){
        Index index = Index.builder().page(page).lemma(lemma).rank(rankLemma).build();
        indexRepository.save(index);
    }
}
