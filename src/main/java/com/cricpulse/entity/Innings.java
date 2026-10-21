package com.cricpulse.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "innings")
public class Innings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "match_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CricketMatch match;

    @Column(nullable = false)
    private Integer inningsNumber; // 1 or 2

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batting_team_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team battingTeam;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bowling_team_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team bowlingTeam;

    private Integer totalRuns = 0;
    private Integer wickets = 0;
    private Integer legalBalls = 0;

    private Integer extrasWide = 0;
    private Integer extrasNoBall = 0;
    private Integer extrasBye = 0;
    private Integer extrasLegBye = 0;
    private Integer extrasPenalty = 0;

    private Integer targetRuns; // Applicable for 2nd innings

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_striker_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player currentStriker;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_non_striker_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player currentNonStriker;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_bowler_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Player currentBowler;

    private Boolean isCompleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Innings() {}

    public Innings(CricketMatch match, Integer inningsNumber, Team battingTeam, Team bowlingTeam) {
        this.match = match;
        this.inningsNumber = inningsNumber;
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper calculations
    @Transient
    public String getOversFormatted() {
        if (legalBalls == null || legalBalls <= 0) return "0.0";
        int overs = legalBalls / 6;
        int balls = legalBalls % 6;
        return overs + "." + balls;
    }

    @Transient
    public int getTotalExtras() {
        return (extrasWide != null ? extrasWide : 0)
                + (extrasNoBall != null ? extrasNoBall : 0)
                + (extrasBye != null ? extrasBye : 0)
                + (extrasLegBye != null ? extrasLegBye : 0)
                + (extrasPenalty != null ? extrasPenalty : 0);
    }

    @Transient
    public double getCurrentRunRate() {
        if (legalBalls == null || legalBalls <= 0 || totalRuns == null || totalRuns <= 0) return 0.0;
        double overs = (legalBalls / 6) + ((legalBalls % 6) / 6.0);
        return overs > 0 ? Math.round((totalRuns / overs) * 100.0) / 100.0 : 0.0;
    }

    @Transient
    public Double getRequiredRunRate() {
        if (targetRuns == null || targetRuns <= 0) return null;
        int maxLegalBalls = (match != null && match.getTotalOvers() != null ? match.getTotalOvers() : 20) * 6;
        int remainingBalls = maxLegalBalls - (legalBalls != null ? legalBalls : 0);
        int runsNeeded = targetRuns - (totalRuns != null ? totalRuns : 0);
        if (runsNeeded <= 0) return 0.0;
        if (remainingBalls <= 0) return 99.99;
        double remainingOvers = remainingBalls / 6.0;
        return Math.round((runsNeeded / remainingOvers) * 100.0) / 100.0;
    }

    // Standard Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CricketMatch getMatch() { return match; }
    public void setMatch(CricketMatch match) { this.match = match; }

    public Integer getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(Integer inningsNumber) { this.inningsNumber = inningsNumber; }

    public Team getBattingTeam() { return battingTeam; }
    public void setBattingTeam(Team battingTeam) { this.battingTeam = battingTeam; }

    public Team getBowlingTeam() { return bowlingTeam; }
    public void setBowlingTeam(Team bowlingTeam) { this.bowlingTeam = bowlingTeam; }

    public Integer getTotalRuns() { return totalRuns != null ? totalRuns : 0; }
    public void setTotalRuns(Integer totalRuns) { this.totalRuns = totalRuns; }

    public Integer getWickets() { return wickets != null ? wickets : 0; }
    public void setWickets(Integer wickets) { this.wickets = wickets; }

    public Integer getLegalBalls() { return legalBalls != null ? legalBalls : 0; }
    public void setLegalBalls(Integer legalBalls) { this.legalBalls = legalBalls; }

    public Integer getExtrasWide() { return extrasWide != null ? extrasWide : 0; }
    public void setExtrasWide(Integer extrasWide) { this.extrasWide = extrasWide; }

    public Integer getExtrasNoBall() { return extrasNoBall != null ? extrasNoBall : 0; }
    public void setExtrasNoBall(Integer extrasNoBall) { this.extrasNoBall = extrasNoBall; }

    public Integer getExtrasBye() { return extrasBye != null ? extrasBye : 0; }
    public void setExtrasBye(Integer extrasBye) { this.extrasBye = extrasBye; }

    public Integer getExtrasLegBye() { return extrasLegBye != null ? extrasLegBye : 0; }
    public void setExtrasLegBye(Integer extrasLegBye) { this.extrasLegBye = extrasLegBye; }

    public Integer getExtrasPenalty() { return extrasPenalty != null ? extrasPenalty : 0; }
    public void setExtrasPenalty(Integer extrasPenalty) { this.extrasPenalty = extrasPenalty; }

    public Integer getTargetRuns() { return targetRuns; }
    public void setTargetRuns(Integer targetRuns) { this.targetRuns = targetRuns; }

    public Player getCurrentStriker() { return currentStriker; }
    public void setCurrentStriker(Player currentStriker) { this.currentStriker = currentStriker; }

    public Player getCurrentNonStriker() { return currentNonStriker; }
    public void setCurrentNonStriker(Player currentNonStriker) { this.currentNonStriker = currentNonStriker; }

    public Player getCurrentBowler() { return currentBowler; }
    public void setCurrentBowler(Player currentBowler) { this.currentBowler = currentBowler; }

    public Boolean getIsCompleted() { return isCompleted != null ? isCompleted : false; }
    public void setIsCompleted(Boolean completed) { isCompleted = completed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
