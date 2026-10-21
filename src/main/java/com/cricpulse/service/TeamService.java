package com.cricpulse.service;

import com.cricpulse.dto.PlayerDto;
import com.cricpulse.dto.TeamDto;
import com.cricpulse.entity.Player;
import com.cricpulse.entity.Team;
import com.cricpulse.exception.ResourceNotFoundException;
import com.cricpulse.repository.PlayerRepository;
import com.cricpulse.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamDto> getAllTeams() {
        return teamRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamDto getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + id));
        return mapToDto(team);
    }

    @Transactional
    public Team createTeam(Team team) {
        return teamRepository.save(team);
    }

    @Transactional
    public Team updateTeam(Long id, Team updated) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + id));
        team.setName(updated.getName());
        team.setShortName(updated.getShortName());
        team.setLogoUrl(updated.getLogoUrl());
        team.setPrimaryColor(updated.getPrimaryColor());
        return teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team not found with ID: " + id);
        }
        teamRepository.deleteById(id);
    }

    private TeamDto mapToDto(Team team) {
        TeamDto dto = new TeamDto();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setShortName(team.getShortName());
        dto.setLogoUrl(team.getLogoUrl());
        dto.setPrimaryColor(team.getPrimaryColor());

        List<Player> players = playerRepository.findByTeamIdOrderByNameAsc(team.getId());
        dto.setPlayers(players.stream().map(p -> {
            PlayerDto pd = new PlayerDto();
            pd.setId(p.getId());
            pd.setName(p.getName());
            pd.setTeamId(team.getId());
            pd.setTeamName(team.getName());
            pd.setRole(p.getRole());
            pd.setBattingStyle(p.getBattingStyle());
            pd.setBowlingStyle(p.getBowlingStyle());
            pd.setJerseyNumber(p.getJerseyNumber());
            return pd;
        }).collect(Collectors.toList()));

        return dto;
    }
}
