package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    List<Page> findPageByPath(String path);
    Long countBySiteId(Long siteId);
}
