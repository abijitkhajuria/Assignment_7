package com.cricpulse.service;

import com.cricpulse.dto.AdminLoginRequestDto;
import com.cricpulse.entity.AdminUser;
import com.cricpulse.entity.CricketMatch;
import com.cricpulse.entity.MatchStatus;
import com.cricpulse.entity.Team;
import com.cricpulse.exception.BadRequestException;
import com.cricpulse.exception.ResourceNotFoundException;
import com.cricpulse.repository.AdminUserRepository;
import com.cricpulse.repository.MatchRepository;
import com.cricpulse.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {

    private final AdminUserRepository adminUserRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public AdminService(
            AdminUserRepository adminUserRepository,
            MatchRepository matchRepository,
            TeamRepository teamRepository) {
        this.adminUserRepository = adminUserRepository;
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
    }

    public Map<String, Object> authenticate(AdminLoginRequestDto request) {
        Optional<AdminUser> userOpt = adminUserRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(request.getPassword())) {
            throw new BadRequestException("Invalid admin username or password.");
        }

        AdminUser user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", "cricpulse-auth-token-admin-" + user.getId());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());
        return response;
    }

    @Transactional
    public CricketMatch startMatch(Long matchId) {
        CricketMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));
        match.setStatus(MatchStatus.LIVE);
        return matchRepository.save(match);
    }

    @Transactional
    public CricketMatch finishMatch(Long matchId, String resultMargin, Long winnerTeamId) {
        CricketMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));

        match.setStatus(MatchStatus.COMPLETED);
        if (resultMargin != null && !resultMargin.trim().isEmpty()) {
            match.setResultMargin(resultMargin);
        }
        if (winnerTeamId != null) {
            Team winner = teamRepository.findById(winnerTeamId).orElse(null);
            match.setWinnerTeam(winner);
        }
        return matchRepository.save(match);
    }

    @Transactional
    public void deleteMatch(Long matchId) {
        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException("Match not found: " + matchId);
        }
        matchRepository.deleteById(matchId);
    }
}
