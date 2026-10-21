package com.cricpulse.dto;

import com.cricpulse.entity.MatchFormat;
import com.cricpulse.entity.MatchStatus;
import java.util.List;

public class ScorecardResponseDto {
    private Long matchId;
    private String title;
    private String venue;
    private MatchFormat format;
    private MatchStatus status;
    private String resultMargin;
    private String matchSummary;
    private String tossInfo;
    private String playerOfTheMatchName;
    private List<InningsScorecardDto> innings;

    public ScorecardResponseDto() {}

    public static class InningsScorecardDto {
        private Long inningsId;
        private Integer inningsNumber;
        private String battingTeamName;
        private String battingTeamShortName;
        private String bowlingTeamName;
        private String bowlingTeamShortName;
        private Integer totalRuns;
        private Integer wickets;
        private String oversFormatted;
        private Double runRate;

        // Extras
        private Integer extrasTotal;
        private Integer extrasWide;
        private Integer extrasNoBall;
        private Integer extrasBye;
        private Integer extrasLegBye;

        // Details
        private List<BattingCardRowDto> battingCards;
        private List<String> didNotBat;
        private List<BowlingCardRowDto> bowlingCards;
        private List<FallOfWicketDto> fallOfWickets;

        public InningsScorecardDto() {}

        // Getters and setters
        public Long getInningsId() { return inningsId; }
        public void setInningsId(Long inningsId) { this.inningsId = inningsId; }
        public Integer getInningsNumber() { return inningsNumber; }
        public void setInningsNumber(Integer inningsNumber) { this.inningsNumber = inningsNumber; }
        public String getBattingTeamName() { return battingTeamName; }
        public void setBattingTeamName(String battingTeamName) { this.battingTeamName = battingTeamName; }
        public String getBattingTeamShortName() { return battingTeamShortName; }
        public void setBattingTeamShortName(String battingTeamShortName) { this.battingTeamShortName = battingTeamShortName; }
        public String getBowlingTeamName() { return bowlingTeamName; }
        public void setBowlingTeamName(String bowlingTeamName) { this.bowlingTeamName = bowlingTeamName; }
        public String getBowlingTeamShortName() { return bowlingTeamShortName; }
        public void setBowlingTeamShortName(String bowlingTeamShortName) { this.bowlingTeamShortName = bowlingTeamShortName; }
        public Integer getTotalRuns() { return totalRuns; }
        public void setTotalRuns(Integer totalRuns) { this.totalRuns = totalRuns; }
        public Integer getWickets() { return wickets; }
        public void setWickets(Integer wickets) { this.wickets = wickets; }
        public String getOversFormatted() { return oversFormatted; }
        public void setOversFormatted(String oversFormatted) { this.oversFormatted = oversFormatted; }
        public Double getRunRate() { return runRate; }
        public void setRunRate(Double runRate) { this.runRate = runRate; }
        public Integer getExtrasTotal() { return extrasTotal; }
        public void setExtrasTotal(Integer extrasTotal) { this.extrasTotal = extrasTotal; }
        public Integer getExtrasWide() { return extrasWide; }
        public void setExtrasWide(Integer extrasWide) { this.extrasWide = extrasWide; }
        public Integer getExtrasNoBall() { return extrasNoBall; }
        public void setExtrasNoBall(Integer extrasNoBall) { this.extrasNoBall = extrasNoBall; }
        public Integer getExtrasBye() { return extrasBye; }
        public void setExtrasBye(Integer extrasBye) { this.extrasBye = extrasBye; }
        public Integer getExtrasLegBye() { return extrasLegBye; }
        public void setExtrasLegBye(Integer extrasLegBye) { this.extrasLegBye = extrasLegBye; }
        public List<BattingCardRowDto> getBattingCards() { return battingCards; }
        public void setBattingCards(List<BattingCardRowDto> battingCards) { this.battingCards = battingCards; }
        public List<String> getDidNotBat() { return didNotBat; }
        public void setDidNotBat(List<String> didNotBat) { this.didNotBat = didNotBat; }
        public List<BowlingCardRowDto> getBowlingCards() { return bowlingCards; }
        public void setBowlingCards(List<BowlingCardRowDto> bowlingCards) { this.bowlingCards = bowlingCards; }
        public List<FallOfWicketDto> getFallOfWickets() { return fallOfWickets; }
        public void setFallOfWickets(List<FallOfWicketDto> fallOfWickets) { this.fallOfWickets = fallOfWickets; }
    }

    public static class BattingCardRowDto {
        private Long playerId;
        private String playerName;
        private String dismissalText;
        private Integer runs;
        private Integer ballsFaced;
        private Integer fours;
        private Integer sixes;
        private Double strikeRate;
        private Boolean isOut;

        public BattingCardRowDto() {}
        public BattingCardRowDto(Long playerId, String playerName, String dismissalText, Integer runs, Integer ballsFaced, Integer fours, Integer sixes, Double strikeRate, Boolean isOut) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.dismissalText = dismissalText;
            this.runs = runs;
            this.ballsFaced = ballsFaced;
            this.fours = fours;
            this.sixes = sixes;
            this.strikeRate = strikeRate;
            this.isOut = isOut;
        }

        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }
        public String getDismissalText() { return dismissalText; }
        public void setDismissalText(String dismissalText) { this.dismissalText = dismissalText; }
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
        public Boolean getIsOut() { return isOut; }
        public void setIsOut(Boolean out) { isOut = out; }
    }

    public static class BowlingCardRowDto {
        private Long playerId;
        private String playerName;
        private String oversFormatted;
        private Integer maidens;
        private Integer runsConceded;
        private Integer wickets;
        private Double economy;

        public BowlingCardRowDto() {}
        public BowlingCardRowDto(Long playerId, String playerName, String oversFormatted, Integer maidens, Integer runsConceded, Integer wickets, Double economy) {
            this.playerId = playerId;
            this.playerName = playerName;
            this.oversFormatted = oversFormatted;
            this.maidens = maidens;
            this.runsConceded = runsConceded;
            this.wickets = wickets;
            this.economy = economy;
        }

        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }
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

    public static class FallOfWicketDto {
        private Integer wicketNumber;
        private Integer teamScore;
        private String oversFormatted;
        private String playerName;

        public FallOfWicketDto() {}
        public FallOfWicketDto(Integer wicketNumber, Integer teamScore, String oversFormatted, String playerName) {
            this.wicketNumber = wicketNumber;
            this.teamScore = teamScore;
            this.oversFormatted = oversFormatted;
            this.playerName = playerName;
        }

        public Integer getWicketNumber() { return wicketNumber; }
        public void setWicketNumber(Integer wicketNumber) { this.wicketNumber = wicketNumber; }
        public Integer getTeamScore() { return teamScore; }
        public void setTeamScore(Integer teamScore) { this.teamScore = teamScore; }
        public String getOversFormatted() { return oversFormatted; }
        public void setOversFormatted(String oversFormatted) { this.oversFormatted = oversFormatted; }
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }
    }

    // Main DTO getters and setters
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public MatchFormat getFormat() { return format; }
    public void setFormat(MatchFormat format) { this.format = format; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    public String getResultMargin() { return resultMargin; }
    public void setResultMargin(String resultMargin) { this.resultMargin = resultMargin; }
    public String getMatchSummary() { return matchSummary; }
    public void setMatchSummary(String matchSummary) { this.matchSummary = matchSummary; }
    public String getTossInfo() { return tossInfo; }
    public void setTossInfo(String tossInfo) { this.tossInfo = tossInfo; }
    public String getPlayerOfTheMatchName() { return playerOfTheMatchName; }
    public void setPlayerOfTheMatchName(String playerOfTheMatchName) { this.playerOfTheMatchName = playerOfTheMatchName; }
    public List<InningsScorecardDto> getInnings() { return innings; }
    public void setInnings(List<InningsScorecardDto> innings) { this.innings = innings; }
}
