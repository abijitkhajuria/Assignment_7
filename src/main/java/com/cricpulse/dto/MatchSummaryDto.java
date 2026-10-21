package com.cricpulse.dto;

import com.cricpulse.entity.MatchFormat;
import com.cricpulse.entity.MatchStatus;
import java.time.LocalDateTime;

public class MatchSummaryDto {
    private Long id;
    private String title;
    private String venue;
    private LocalDateTime matchDate;
    private MatchFormat format;
    private Integer totalOvers;
    private MatchStatus status;

    private Long team1Id;
    private String team1Name;
    private String team1ShortName;
    private String team1Color;

    private Long team2Id;
    private String team2Name;
    private String team2ShortName;
    private String team2Color;

    private Integer innings1Runs;
    private Integer innings1Wickets;
    private String innings1Overs;
    private String innings1TeamName;

    private Integer innings2Runs;
    private Integer innings2Wickets;
    private String innings2Overs;
    private String innings2TeamName;

    private Integer targetRuns;
    private Double currentRunRate;
    private Double requiredRunRate;
    private String resultMargin;
    private String currentStatusNote;
    private String seriesName;
    private String dayNote;
    private String dayLabel;
    private String matchTime;
    private boolean isToday;
    private boolean isTomorrow;

    public MatchSummaryDto() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public Long getTeam1Id() { return team1Id; }
    public void setTeam1Id(Long team1Id) { this.team1Id = team1Id; }

    public String getTeam1Name() { return team1Name; }
    public void setTeam1Name(String team1Name) { this.team1Name = team1Name; }

    public String getTeam1ShortName() { return team1ShortName; }
    public void setTeam1ShortName(String team1ShortName) { this.team1ShortName = team1ShortName; }

    public String getTeam1Color() { return team1Color; }
    public void setTeam1Color(String team1Color) { this.team1Color = team1Color; }

    public Long getTeam2Id() { return team2Id; }
    public void setTeam2Id(Long team2Id) { this.team2Id = team2Id; }

    public String getTeam2Name() { return team2Name; }
    public void setTeam2Name(String team2Name) { this.team2Name = team2Name; }

    public String getTeam2ShortName() { return team2ShortName; }
    public void setTeam2ShortName(String team2ShortName) { this.team2ShortName = team2ShortName; }

    public String getTeam2Color() { return team2Color; }
    public void setTeam2Color(String team2Color) { this.team2Color = team2Color; }

    public Integer getInnings1Runs() { return innings1Runs; }
    public void setInnings1Runs(Integer innings1Runs) { this.innings1Runs = innings1Runs; }

    public Integer getInnings1Wickets() { return innings1Wickets; }
    public void setInnings1Wickets(Integer innings1Wickets) { this.innings1Wickets = innings1Wickets; }

    public String getInnings1Overs() { return innings1Overs; }
    public void setInnings1Overs(String innings1Overs) { this.innings1Overs = innings1Overs; }

    public String getInnings1TeamName() { return innings1TeamName; }
    public void setInnings1TeamName(String innings1TeamName) { this.innings1TeamName = innings1TeamName; }

    public Integer getInnings2Runs() { return innings2Runs; }
    public void setInnings2Runs(Integer innings2Runs) { this.innings2Runs = innings2Runs; }

    public Integer getInnings2Wickets() { return innings2Wickets; }
    public void setInnings2Wickets(Integer innings2Wickets) { this.innings2Wickets = innings2Wickets; }

    public String getInnings2Overs() { return innings2Overs; }
    public void setInnings2Overs(String innings2Overs) { this.innings2Overs = innings2Overs; }

    public String getInnings2TeamName() { return innings2TeamName; }
    public void setInnings2TeamName(String innings2TeamName) { this.innings2TeamName = innings2TeamName; }

    public Integer getTargetRuns() { return targetRuns; }
    public void setTargetRuns(Integer targetRuns) { this.targetRuns = targetRuns; }

    public Double getCurrentRunRate() { return currentRunRate; }
    public void setCurrentRunRate(Double currentRunRate) { this.currentRunRate = currentRunRate; }

    public Double getRequiredRunRate() { return requiredRunRate; }
    public void setRequiredRunRate(Double requiredRunRate) { this.requiredRunRate = requiredRunRate; }

    public String getResultMargin() { return resultMargin; }
    public void setResultMargin(String resultMargin) { this.resultMargin = resultMargin; }

    public String getCurrentStatusNote() { return currentStatusNote; }
    public void setCurrentStatusNote(String currentStatusNote) { this.currentStatusNote = currentStatusNote; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    public String getDayNote() { return dayNote; }
    public void setDayNote(String dayNote) { this.dayNote = dayNote; }

    public String getDayLabel() { return dayLabel; }
    public void setDayLabel(String dayLabel) { this.dayLabel = dayLabel; }

    public String getMatchTime() { return matchTime; }
    public void setMatchTime(String matchTime) { this.matchTime = matchTime; }

    public boolean isToday() { return isToday; }
    public void setToday(boolean today) { isToday = today; }

    public boolean isTomorrow() { return isTomorrow; }
    public void setTomorrow(boolean tomorrow) { isTomorrow = tomorrow; }
}
