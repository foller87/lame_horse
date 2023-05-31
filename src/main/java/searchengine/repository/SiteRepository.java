package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.List;
import java.util.Set;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    @Transactional
    List<Site> findByUrl(String url);
    @Transactional
    Set<Site> findBySiteStatus(SiteStatus status);
}
