package com.cricpulse.repository;

import com.cricpulse.entity.BattingScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BattingScoreRepository extends JpaRepository<BattingScore, Long> {
    List<BattingScore> findByInningsIdOrderByBattingOrderAsc(Long inningsId);
    Optional<BattingScore> findByInningsIdAndPlayerId(Long inningsId, Long playerId);
    List<BattingScore> findByPlayerId(Long playerId);
}
