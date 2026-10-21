package com.cricpulse.repository;

import com.cricpulse.entity.CricketMatch;
import com.cricpulse.entity.MatchFormat;
import com.cricpulse.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<CricketMatch, Long> {
    List<CricketMatch> findByStatusOrderByMatchDateDesc(MatchStatus status);
    List<CricketMatch> findByStatusInOrderByMatchDateDesc(List<MatchStatus> statuses);
    List<CricketMatch> findAllByOrderByMatchDateDesc();
    List<CricketMatch> findByFormatOrderByMatchDateDesc(MatchFormat format);
}
