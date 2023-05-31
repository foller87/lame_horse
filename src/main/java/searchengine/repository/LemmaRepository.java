package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    @Transactional
    List<Lemma> findByLemma(String lemma);
    @Modifying
    @Transactional
    @Query( value = "update search_engine.lemma l, `search_engine`.`index` i, `search_engine`." +
            "`pages` p set l.frequency = l.frequency-1 \n" +
            "WHERE i.lemma_id = l.id and p.id = i.pages_id and l.frequency > 0 and p.id = :pageId",
            nativeQuery = true)
    void updateLemmaByPageIdNative(@Param("pageId") Long pageId);
    @Transactional
    long countBySiteId(Long siteId);
    void deleteAllBySite(Site site);
}
