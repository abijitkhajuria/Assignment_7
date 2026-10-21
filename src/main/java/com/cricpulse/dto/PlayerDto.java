package com.cricpulse.dto;

import com.cricpulse.entity.PlayerRole;

public class PlayerDto {
    private Long id;
    private String name;
    private Long teamId;
    private String teamName;
    private PlayerRole role;
    private String battingStyle;
    private String bowlingStyle;
    private Integer jerseyNumber;

    public PlayerDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public PlayerRole getRole() { return role; }
    public void setRole(PlayerRole role) { this.role = role; }
    public String getBattingStyle() { return battingStyle; }
    public void setBattingStyle(String battingStyle) { this.battingStyle = battingStyle; }
    public String getBowlingStyle() { return bowlingStyle; }
    public void setBowlingStyle(String bowlingStyle) { this.bowlingStyle = bowlingStyle; }
    public Integer getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(Integer jerseyNumber) { this.jerseyNumber = jerseyNumber; }
}
