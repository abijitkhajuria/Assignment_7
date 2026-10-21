package com.cricpulse.repository;

import com.cricpulse.entity.Player;
import com.cricpulse.entity.PlayerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamId(Long teamId);
    List<Player> findByTeamIdOrderByNameAsc(Long teamId);
    List<Player> findByRole(PlayerRole role);
}
