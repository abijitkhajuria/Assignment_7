package com.cricpulse.repository;

import com.cricpulse.entity.FallOfWicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FallOfWicketRepository extends JpaRepository<FallOfWicket, Long> {
    List<FallOfWicket> findByInningsIdOrderByWicketNumberAsc(Long inningsId);
    void deleteByInningsIdAndWicketNumberGreaterThanEqual(Long inningsId, Integer wicketNumber);
}
