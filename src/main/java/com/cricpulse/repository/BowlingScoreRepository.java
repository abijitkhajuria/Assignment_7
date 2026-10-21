package com.cricpulse.repository;

import com.cricpulse.entity.BowlingScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BowlingScoreRepository extends JpaRepository<BowlingScore, Long> {
    List<BowlingScore> findByInningsIdOrderByIdAsc(Long inningsId);
    Optional<BowlingScore> findByInningsIdAndPlayerId(Long inningsId, Long playerId);
    List<BowlingScore> findByPlayerId(Long playerId);
}
