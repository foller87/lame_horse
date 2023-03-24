package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import searchengine.model.SiteStatus;

import java.util.List;
import java.util.Set;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    List<Site> findByUrl(String url);
    Set<Site> findBySiteStatus(SiteStatus status);

    @Override
    void deleteAllByIdInBatch(Iterable<Long> longs);
}
