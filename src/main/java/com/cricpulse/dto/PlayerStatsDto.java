package com.cricpulse.dto;

import com.cricpulse.entity.PlayerRole;

public class PlayerStatsDto {
    private Long id;
    private String name;
    private Long teamId;
    private String teamName;
    private String teamShortName;
    private PlayerRole role;
    private String battingStyle;
    private String bowlingStyle;
    private Integer jerseyNumber;
    private String avatarUrl;

    // Career Demo Stats
    private Integer careerMatches;
    private Integer careerRuns;
    private Integer careerHighestScore;
    private Double careerBattingAverage;
    private Double careerStrikeRate;
    private Integer careerWickets;
    private Double careerBowlingAverage;
    private String careerBestBowling;
    private Double careerEconomy;

    // Tournament Live Recorded Stats
    private Integer tournamentMatches;
    private Integer tournamentRuns;
    private Integer tournamentHighestScore;
    private Integer tournamentFours;
    private Integer tournamentSixes;
    private Integer tournamentWickets;
    private Integer tournamentBallsBowled;
    private Integer tournamentRunsConceded;
    private Double tournamentEconomy;

    public PlayerStatsDto() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getTeamShortName() { return teamShortName; }
    public void setTeamShortName(String teamShortName) { this.teamShortName = teamShortName; }
    public PlayerRole getRole() { return role; }
    public void setRole(PlayerRole role) { this.role = role; }
    public String getBattingStyle() { return battingStyle; }
    public void setBattingStyle(String battingStyle) { this.battingStyle = battingStyle; }
    public String getBowlingStyle() { return bowlingStyle; }
    public void setBowlingStyle(String bowlingStyle) { this.bowlingStyle = bowlingStyle; }
    public Integer getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(Integer jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
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
    public Integer getTournamentMatches() { return tournamentMatches; }
    public void setTournamentMatches(Integer tournamentMatches) { this.tournamentMatches = tournamentMatches; }
    public Integer getTournamentRuns() { return tournamentRuns; }
    public void setTournamentRuns(Integer tournamentRuns) { this.tournamentRuns = tournamentRuns; }
    public Integer getTournamentHighestScore() { return tournamentHighestScore; }
    public void setTournamentHighestScore(Integer tournamentHighestScore) { this.tournamentHighestScore = tournamentHighestScore; }
    public Integer getTournamentFours() { return tournamentFours; }
    public void setTournamentFours(Integer tournamentFours) { this.tournamentFours = tournamentFours; }
    public Integer getTournamentSixes() { return tournamentSixes; }
    public void setTournamentSixes(Integer tournamentSixes) { this.tournamentSixes = tournamentSixes; }
    public Integer getTournamentWickets() { return tournamentWickets; }
    public void setTournamentWickets(Integer tournamentWickets) { this.tournamentWickets = tournamentWickets; }
    public Integer getTournamentBallsBowled() { return tournamentBallsBowled; }
    public void setTournamentBallsBowled(Integer tournamentBallsBowled) { this.tournamentBallsBowled = tournamentBallsBowled; }
    public Integer getTournamentRunsConceded() { return tournamentRunsConceded; }
    public void setTournamentRunsConceded(Integer tournamentRunsConceded) { this.tournamentRunsConceded = tournamentRunsConceded; }
    public Double getTournamentEconomy() { return tournamentEconomy; }
    public void setTournamentEconomy(Double tournamentEconomy) { this.tournamentEconomy = tournamentEconomy; }
}
