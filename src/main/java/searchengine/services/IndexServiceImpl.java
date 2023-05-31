package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService{
    private final IndexRepository indexRepository;

    @Override
    public Index saveIndexInRepository(Page page, Lemma lemma, int rankLemma){
        Index index = Index.builder().page(page).lemma(lemma).rank(rankLemma).build();
        return indexRepository.save(index);
    }
    @Override
    public List<Index> findIndexByPageAndLemma(Page page, Lemma lemma){
        List<Index> indexes = indexRepository.findByPageAndLemma(page, lemma);
        return indexes;
    }
    public void saveAllInAndFlush(List<Index> indexes){
        indexRepository.saveAllAndFlush(indexes);
    }
}
