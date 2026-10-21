package com.cricpulse.repository;

import com.cricpulse.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByShortNameIgnoreCase(String shortName);
    Optional<Team> findByNameIgnoreCase(String name);
}
