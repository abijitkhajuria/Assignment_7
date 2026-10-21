package com.cricpulse.service;

import com.cricpulse.dto.PlayerDto;
import com.cricpulse.dto.PlayerStatsDto;
import com.cricpulse.entity.BattingScore;
import com.cricpulse.entity.BowlingScore;
import com.cricpulse.entity.Player;
import com.cricpulse.exception.ResourceNotFoundException;
import com.cricpulse.repository.BattingScoreRepository;
import com.cricpulse.repository.BowlingScoreRepository;
import com.cricpulse.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final BattingScoreRepository battingScoreRepository;
    private final BowlingScoreRepository bowlingScoreRepository;

    public PlayerService(
            PlayerRepository playerRepository,
            BattingScoreRepository battingScoreRepository,
            BowlingScoreRepository bowlingScoreRepository) {
        this.playerRepository = playerRepository;
        this.battingScoreRepository = battingScoreRepository;
        this.bowlingScoreRepository = bowlingScoreRepository;
    }

    @Transactional(readOnly = true)
    public List<PlayerDto> getAllPlayers(Long teamId) {
        List<Player> players = (teamId != null)
                ? playerRepository.findByTeamIdOrderByNameAsc(teamId)
                : playerRepository.findAll();
        return players.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlayerDto getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + id));
        return mapToDto(player);
    }

    @Transactional(readOnly = true)
    public PlayerStatsDto getPlayerStatistics(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + id));

        PlayerStatsDto dto = new PlayerStatsDto();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setTeamId(player.getTeam().getId());
        dto.setTeamName(player.getTeam().getName());
        dto.setTeamShortName(player.getTeam().getShortName());
        dto.setRole(player.getRole());
        dto.setBattingStyle(player.getBattingStyle());
        dto.setBowlingStyle(player.getBowlingStyle());
        dto.setJerseyNumber(player.getJerseyNumber());
        dto.setAvatarUrl(player.getAvatarUrl());

        // Career Demo Stats
        dto.setCareerMatches(player.getCareerMatches());
        dto.setCareerRuns(player.getCareerRuns());
        dto.setCareerHighestScore(player.getCareerHighestScore());
        dto.setCareerBattingAverage(player.getCareerBattingAverage());
        dto.setCareerStrikeRate(player.getCareerStrikeRate());
        dto.setCareerWickets(player.getCareerWickets());
        dto.setCareerBowlingAverage(player.getCareerBowlingAverage());
        dto.setCareerBestBowling(player.getCareerBestBowling());
        dto.setCareerEconomy(player.getCareerEconomy());

        // Tournament Recorded Stats (calculated live from BattingScore & BowlingScore entities)
        List<BattingScore> battingScores = battingScoreRepository.findByPlayerId(player.getId());
        int tourneyRuns = 0;
        int tourneyHigh = 0;
        int tourneyFours = 0;
        int tourneySixes = 0;
        for (BattingScore bs : battingScores) {
            tourneyRuns += bs.getRuns();
            if (bs.getRuns() > tourneyHigh) tourneyHigh = bs.getRuns();
            tourneyFours += bs.getFours();
            tourneySixes += bs.getSixes();
        }

        List<BowlingScore> bowlingScores = bowlingScoreRepository.findByPlayerId(player.getId());
        int tourneyWickets = 0;
        int tourneyBalls = 0;
        int tourneyRunsConceded = 0;
        for (BowlingScore bs : bowlingScores) {
            tourneyWickets += bs.getWickets();
            tourneyBalls += bs.getLegalBallsBowled();
            tourneyRunsConceded += bs.getRunsConceded();
        }

        dto.setTournamentMatches(Math.max(battingScores.size(), bowlingScores.size()));
        dto.setTournamentRuns(tourneyRuns);
        dto.setTournamentHighestScore(tourneyHigh);
        dto.setTournamentFours(tourneyFours);
        dto.setTournamentSixes(tourneySixes);
        dto.setTournamentWickets(tourneyWickets);
        dto.setTournamentBallsBowled(tourneyBalls);
        dto.setTournamentRunsConceded(tourneyRunsConceded);
        if (tourneyBalls > 0) {
            double overs = (tourneyBalls / 6) + ((tourneyBalls % 6) / 6.0);
            dto.setTournamentEconomy(Math.round((tourneyRunsConceded / overs) * 100.0) / 100.0);
        } else {
            dto.setTournamentEconomy(0.0);
        }

        return dto;
    }

    private PlayerDto mapToDto(Player p) {
        PlayerDto dto = new PlayerDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setTeamId(p.getTeam().getId());
        dto.setTeamName(p.getTeam().getName());
        dto.setRole(p.getRole());
        dto.setBattingStyle(p.getBattingStyle());
        dto.setBowlingStyle(p.getBowlingStyle());
        dto.setJerseyNumber(p.getJerseyNumber());
        return dto;
    }
}
