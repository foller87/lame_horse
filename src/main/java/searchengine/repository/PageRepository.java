package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    @Transactional
    List<Page> findPageByPath(String path);
    @Transactional
    Long countBySiteId(Long siteId);
    @Transactional
    @Query(value = "select * from pages p\n" +
            "join `index` i  on p.id = i.pages_id\n" +
            "where i.lemma_id = :lemmaId", nativeQuery = true)
    List<Page> findByLemmaId(@Param("lemmaId") Long lemmaId);
}
