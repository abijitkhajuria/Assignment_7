package com.cricpulse.dto;

import com.cricpulse.entity.MatchFormat;
import com.cricpulse.entity.TossDecision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateMatchRequestDto {
    @NotBlank(message = "Match title is required")
    private String title;

    @NotBlank(message = "Venue is required")
    private String venue;

    private LocalDateTime matchDate;

    private MatchFormat format = MatchFormat.T20;

    private Integer totalOvers = 20;

    @NotNull(message = "Team 1 ID is required")
    private Long team1Id;

    @NotNull(message = "Team 2 ID is required")
    private Long team2Id;

    private Long tossWinnerId;

    private TossDecision tossDecision = TossDecision.BAT;

    public CreateMatchRequestDto() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }
    public MatchFormat getFormat() { return format; }
    public void setFormat(MatchFormat format) { this.format = format; }
    public Integer getTotalOvers() { return totalOvers; }
    public void setTotalOvers(Integer totalOvers) { this.totalOvers = totalOvers; }
    public Long getTeam1Id() { return team1Id; }
    public void setTeam1Id(Long team1Id) { this.team1Id = team1Id; }
    public Long getTeam2Id() { return team2Id; }
    public void setTeam2Id(Long team2Id) { this.team2Id = team2Id; }
    public Long getTossWinnerId() { return tossWinnerId; }
    public void setTossWinnerId(Long tossWinnerId) { this.tossWinnerId = tossWinnerId; }
    public TossDecision getTossDecision() { return tossDecision; }
    public void setTossDecision(TossDecision tossDecision) { this.tossDecision = tossDecision; }
}
