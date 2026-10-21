package com.cricpulse.dto;

import com.cricpulse.entity.MatchFormat;
import com.cricpulse.entity.MatchStatus;
import java.util.List;

public class LiveMatchSnapshotDto {
    private Long matchId;
    private String title;
    private String venue;
    private MatchFormat format;
    private Integer totalOvers;
    private MatchStatus status;
    private String tossInfo;
    private String resultMargin;
    private String matchSummary;

    // Current Innings Info
    private Long inningsId;
    private Integer inningsNumber;
    private Long battingTeamId;
    private String battingTeamName;
    private String battingTeamShortName;
    private String battingTeamColor;

    private Long bowlingTeamId;
    private String bowlingTeamName;
    private String bowlingTeamShortName;
    private String bowlingTeamColor;

    private Integer totalRuns;
    private Integer wickets;
    private String oversFormatted;
    private Integer legalBalls;
    private Double currentRunRate;
    private Double requiredRunRate;
    private Integer targetRuns;
    private Integer runsNeeded;
    private Integer remainingBalls;

    // Active Players
    private BatterLiveDto striker;
    private BatterLiveDto nonStriker;
    private BowlerLiveDto currentBowler;

    // Recent deliveries & commentary
    private List<RecentBallDto> recentBalls;
    private List<CommentaryItemDto> recentCommentary;

    // Status flags
    private boolean inningsCompleted;
    private boolean matchCompleted;

    public LiveMatchSnapshotDto() {}

    // Sub-classes for clean structure
    public static class BatterLiveDto {
        private Long playerId;
        private String name;
        private Integer runs;
        private Integer ballsFaced;
        private Integer fours;
        private Integer sixes;
        private Double strikeRate;

        public BatterLiveDto() {}
        public BatterLiveDto(Long playerId, String name, Integer runs, Integer ballsFaced, Integer fours, Integer sixes, Double strikeRate) {
            this.playerId = playerId;
            this.name = name;
            this.runs = runs;
            this.ballsFaced = ballsFaced;
            this.fours = fours;
            this.sixes = sixes;
            this.strikeRate = strikeRate;
        }

        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getRuns() { return runs; }
        public void setRuns(Integer runs) { this.runs = runs; }
        public Integer getBallsFaced() { return ballsFaced; }
        public void setBallsFaced(Integer ballsFaced) { this.ballsFaced = ballsFaced; }
        public Integer getFours() { return fours; }
        public void setFours(Integer fours) { this.fours = fours; }
        public Integer getSixes() { return sixes; }
        public void setSixes(Integer sixes) { this.sixes = sixes; }
        public Double getStrikeRate() { return strikeRate; }
        public void setStrikeRate(Double strikeRate) { this.strikeRate = strikeRate; }
    }

    public static class BowlerLiveDto {
        private Long playerId;
        private String name;
        private String oversFormatted;
        private Integer maidens;
        private Integer runsConceded;
        private Integer wickets;
        private Double economy;

        public BowlerLiveDto() {}
        public BowlerLiveDto(Long playerId, String name, String oversFormatted, Integer maidens, Integer runsConceded, Integer wickets, Double economy) {
            this.playerId = playerId;
            this.name = name;
            this.oversFormatted = oversFormatted;
            this.maidens = maidens;
            this.runsConceded = runsConceded;
            this.wickets = wickets;
            this.economy = economy;
        }

        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOversFormatted() { return oversFormatted; }
        public void setOversFormatted(String oversFormatted) { this.oversFormatted = oversFormatted; }
        public Integer getMaidens() { return maidens; }
        public void setMaidens(Integer maidens) { this.maidens = maidens; }
        public Integer getRunsConceded() { return runsConceded; }
        public void setRunsConceded(Integer runsConceded) { this.runsConceded = runsConceded; }
        public Integer getWickets() { return wickets; }
        public void setWickets(Integer wickets) { this.wickets = wickets; }
        public Double getEconomy() { return economy; }
        public void setEconomy(Double economy) { this.economy = economy; }
    }

    public static class RecentBallDto {
        private Long deliveryId;
        private String ballDisplay; // e.g. "1", "4", "6", "W", "1wd", "0"
        private String overAndBall; // e.g. "18.2"
        private boolean isBoundary;
        private boolean isWicket;

        public RecentBallDto() {}
        public RecentBallDto(Long deliveryId, String ballDisplay, String overAndBall, boolean isBoundary, boolean isWicket) {
            this.deliveryId = deliveryId;
            this.ballDisplay = ballDisplay;
            this.overAndBall = overAndBall;
            this.isBoundary = isBoundary;
            this.isWicket = isWicket;
        }

        public Long getDeliveryId() { return deliveryId; }
        public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }
        public String getBallDisplay() { return ballDisplay; }
        public void setBallDisplay(String ballDisplay) { this.ballDisplay = ballDisplay; }
        public String getOverAndBall() { return overAndBall; }
        public void setOverAndBall(String overAndBall) { this.overAndBall = overAndBall; }
        public boolean isBoundary() { return isBoundary; }
        public void setBoundary(boolean boundary) { isBoundary = boundary; }
        public boolean isWicket() { return isWicket; }
        public void setWicket(boolean wicket) { isWicket = wicket; }
    }

    public static class CommentaryItemDto {
        private String overFormatted;
        private String text;
        private String badge; // "4", "6", "W", etc.

        public CommentaryItemDto() {}
        public CommentaryItemDto(String overFormatted, String text, String badge) {
            this.overFormatted = overFormatted;
            this.text = text;
            this.badge = badge;
        }

        public String getOverFormatted() { return overFormatted; }
        public void setOverFormatted(String overFormatted) { this.overFormatted = overFormatted; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getBadge() { return badge; }
        public void setBadge(String badge) { this.badge = badge; }
    }

    // Getters and Setters
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public MatchFormat getFormat() { return format; }
    public void setFormat(MatchFormat format) { this.format = format; }

    public Integer getTotalOvers() { return totalOvers; }
    public void setTotalOvers(Integer totalOvers) { this.totalOvers = totalOvers; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public String getTossInfo() { return tossInfo; }
    public void setTossInfo(String tossInfo) { this.tossInfo = tossInfo; }

    public String getResultMargin() { return resultMargin; }
    public void setResultMargin(String resultMargin) { this.resultMargin = resultMargin; }

    public String getMatchSummary() { return matchSummary; }
    public void setMatchSummary(String matchSummary) { this.matchSummary = matchSummary; }

    public Long getInningsId() { return inningsId; }
    public void setInningsId(Long inningsId) { this.inningsId = inningsId; }

    public Integer getInningsNumber() { return inningsNumber; }
    public void setInningsNumber(Integer inningsNumber) { this.inningsNumber = inningsNumber; }

    public Long getBattingTeamId() { return battingTeamId; }
    public void setBattingTeamId(Long battingTeamId) { this.battingTeamId = battingTeamId; }

    public String getBattingTeamName() { return battingTeamName; }
    public void setBattingTeamName(String battingTeamName) { this.battingTeamName = battingTeamName; }

    public String getBattingTeamShortName() { return battingTeamShortName; }
    public void setBattingTeamShortName(String battingTeamShortName) { this.battingTeamShortName = battingTeamShortName; }

    public String getBattingTeamColor() { return battingTeamColor; }
    public void setBattingTeamColor(String battingTeamColor) { this.battingTeamColor = battingTeamColor; }

    public Long getBowlingTeamId() { return bowlingTeamId; }
    public void setBowlingTeamId(Long bowlingTeamId) { this.bowlingTeamId = bowlingTeamId; }

    public String getBowlingTeamName() { return bowlingTeamName; }
    public void setBowlingTeamName(String bowlingTeamName) { this.bowlingTeamName = bowlingTeamName; }

    public String getBowlingTeamShortName() { return bowlingTeamShortName; }
    public void setBowlingTeamShortName(String bowlingTeamShortName) { this.bowlingTeamShortName = bowlingTeamShortName; }

    public String getBowlingTeamColor() { return bowlingTeamColor; }
    public void setBowlingTeamColor(String bowlingTeamColor) { this.bowlingTeamColor = bowlingTeamColor; }

    public Integer getTotalRuns() { return totalRuns; }
    public void setTotalRuns(Integer totalRuns) { this.totalRuns = totalRuns; }

    public Integer getWickets() { return wickets; }
    public void setWickets(Integer wickets) { this.wickets = wickets; }

    public String getOversFormatted() { return oversFormatted; }
    public void setOversFormatted(String oversFormatted) { this.oversFormatted = oversFormatted; }

    public Integer getLegalBalls() { return legalBalls; }
    public void setLegalBalls(Integer legalBalls) { this.legalBalls = legalBalls; }

    public Double getCurrentRunRate() { return currentRunRate; }
    public void setCurrentRunRate(Double currentRunRate) { this.currentRunRate = currentRunRate; }

    public Double getRequiredRunRate() { return requiredRunRate; }
    public void setRequiredRunRate(Double requiredRunRate) { this.requiredRunRate = requiredRunRate; }

    public Integer getTargetRuns() { return targetRuns; }
    public void setTargetRuns(Integer targetRuns) { this.targetRuns = targetRuns; }

    public Integer getRunsNeeded() { return runsNeeded; }
    public void setRunsNeeded(Integer runsNeeded) { this.runsNeeded = runsNeeded; }

    public Integer getRemainingBalls() { return remainingBalls; }
    public void setRemainingBalls(Integer remainingBalls) { this.remainingBalls = remainingBalls; }

    public BatterLiveDto getStriker() { return striker; }
    public void setStriker(BatterLiveDto striker) { this.striker = striker; }

    public BatterLiveDto getNonStriker() { return nonStriker; }
    public void setNonStriker(BatterLiveDto nonStriker) { this.nonStriker = nonStriker; }

    public BowlerLiveDto getCurrentBowler() { return currentBowler; }
    public void setCurrentBowler(BowlerLiveDto currentBowler) { this.currentBowler = currentBowler; }

    public List<RecentBallDto> getRecentBalls() { return recentBalls; }
    public void setRecentBalls(List<RecentBallDto> recentBalls) { this.recentBalls = recentBalls; }

    public List<CommentaryItemDto> getRecentCommentary() { return recentCommentary; }
    public void setRecentCommentary(List<CommentaryItemDto> recentCommentary) { this.recentCommentary = recentCommentary; }

    public boolean isInningsCompleted() { return inningsCompleted; }
    public void setInningsCompleted(boolean inningsCompleted) { this.inningsCompleted = inningsCompleted; }

    public boolean isMatchCompleted() { return matchCompleted; }
    public void setMatchCompleted(boolean matchCompleted) { this.matchCompleted = matchCompleted; }
}
