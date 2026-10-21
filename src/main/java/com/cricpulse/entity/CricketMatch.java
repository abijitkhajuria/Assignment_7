package com.cricpulse.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class CricketMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 150)
    private String seriesName;

    @Column(length = 150)
    private String dayNote;

    @Column(nullable = false, length = 100)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime matchDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchFormat format = MatchFormat.T20;

    @Column(nullable = false)
    private Integer totalOvers = 20;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team1_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team team1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team2_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team team2;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "toss_winner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team tossWinner;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TossDecision tossDecision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStatus status = MatchStatus.SCHEDULED;

    private Integer currentInningsNumber = 1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "winner_team_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team winnerTeam;

    @Column(length = 150)
    private String resultMargin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_of_match_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player playerOfTheMatch;

    @Column(columnDefinition = "TEXT")
    private String matchSummary;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public CricketMatch() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    public String getDayNote() { return dayNote; }
    public void setDayNote(String dayNote) { this.dayNote = dayNote; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }

    public MatchFormat getFormat() { return format; }
    public void setFormat(MatchFormat format) { this.format = format; }

    public Integer getTotalOvers() { return totalOvers; }
    public void setTotalOvers(Integer totalOvers) { this.totalOvers = totalOvers; }

    public Team getTeam1() { return team1; }
    public void setTeam1(Team team1) { this.team1 = team1; }

    public Team getTeam2() { return team2; }
    public void setTeam2(Team team2) { this.team2 = team2; }

    public Team getTossWinner() { return tossWinner; }
    public void setTossWinner(Team tossWinner) { this.tossWinner = tossWinner; }

    public TossDecision getTossDecision() { return tossDecision; }
    public void setTossDecision(TossDecision tossDecision) { this.tossDecision = tossDecision; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public Integer getCurrentInningsNumber() { return currentInningsNumber; }
    public void setCurrentInningsNumber(Integer currentInningsNumber) { this.currentInningsNumber = currentInningsNumber; }

    public Team getWinnerTeam() { return winnerTeam; }
    public void setWinnerTeam(Team winnerTeam) { this.winnerTeam = winnerTeam; }

    public String getResultMargin() { return resultMargin; }
    public void setResultMargin(String resultMargin) { this.resultMargin = resultMargin; }

    public Player getPlayerOfTheMatch() { return playerOfTheMatch; }
    public void setPlayerOfTheMatch(Player playerOfTheMatch) { this.playerOfTheMatch = playerOfTheMatch; }

    public String getMatchSummary() { return matchSummary; }
    public void setMatchSummary(String matchSummary) { this.matchSummary = matchSummary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
