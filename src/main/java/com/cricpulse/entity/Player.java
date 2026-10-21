package com.cricpulse.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlayerRole role;

    @Column(length = 50)
    private String battingStyle;

    @Column(length = 50)
    private String bowlingStyle;

    private Integer jerseyNumber;

    // Career Demo Statistics
    private Integer careerMatches = 0;
    private Integer careerRuns = 0;
    private Integer careerHighestScore = 0;
    private Double careerBattingAverage = 0.0;
    private Double careerStrikeRate = 0.0;
    private Integer careerWickets = 0;
    private Double careerBowlingAverage = 0.0;
    private String careerBestBowling = "-";
    private Double careerEconomy = 0.0;

    @Column(length = 255)
    private String avatarUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Player() {}

    public Player(String name, Team team, PlayerRole role, String battingStyle, String bowlingStyle, Integer jerseyNumber) {
        this.name = name;
        this.team = team;
        this.role = role;
        this.battingStyle = battingStyle;
        this.bowlingStyle = bowlingStyle;
        this.jerseyNumber = jerseyNumber;
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public PlayerRole getRole() { return role; }
    public void setRole(PlayerRole role) { this.role = role; }

    public String getBattingStyle() { return battingStyle; }
    public void setBattingStyle(String battingStyle) { this.battingStyle = battingStyle; }

    public String getBowlingStyle() { return bowlingStyle; }
    public void setBowlingStyle(String bowlingStyle) { this.bowlingStyle = bowlingStyle; }

    public Integer getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(Integer jerseyNumber) { this.jerseyNumber = jerseyNumber; }

    public Integer getCareerMatches() { return careerMatches; }
    public void setCareerMatches(Integer careerMatches) { this.careerMatches = careerMatches; }

    public Integer getCareerRuns() { return careerRuns; }
    public void setCareerRuns(Integer careerRuns) { this.careerRuns = careerRuns; }

    public Integer getCareerHighestScore() { return careerHighestScore; }
    public void setCareerHighestScore(Integer careerHighestScore) { this.careerHighestScore = careerHighestScore; }

    public Double getCareerBattingAverage() { return careerBattingAverage; }
    public void setCareerBattingAverage(Double careerBattingAverage) { this.careerBattingAverage = careerBattingAverage; }

    public Double getCareerStrikeRate() { return careerStrikeRate; }
    public void setCareerStrikeRate(Double careerStrikeRate) { this.careerStrikeRate = careerStrikeRate; }

    public Integer getCareerWickets() { return careerWickets; }
    public void setCareerWickets(Integer careerWickets) { this.careerWickets = careerWickets; }

    public Double getCareerBowlingAverage() { return careerBowlingAverage; }
    public void setCareerBowlingAverage(Double careerBowlingAverage) { this.careerBowlingAverage = careerBowlingAverage; }

    public String getCareerBestBowling() { return careerBestBowling; }
    public void setCareerBestBowling(String careerBestBowling) { this.careerBestBowling = careerBestBowling; }

    public Double getCareerEconomy() { return careerEconomy; }
    public void setCareerEconomy(Double careerEconomy) { this.careerEconomy = careerEconomy; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
