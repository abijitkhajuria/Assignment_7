package com.cricpulse.service;

import com.cricpulse.dto.*;
import com.cricpulse.entity.*;
import com.cricpulse.exception.BadRequestException;
import com.cricpulse.exception.ResourceNotFoundException;
import com.cricpulse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final InningsRepository inningsRepository;
    private final BattingScoreRepository battingScoreRepository;
    private final BowlingScoreRepository bowlingScoreRepository;
    private final FallOfWicketRepository fallOfWicketRepository;

    public MatchService(
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            PlayerRepository playerRepository,
            InningsRepository inningsRepository,
            BattingScoreRepository battingScoreRepository,
            BowlingScoreRepository bowlingScoreRepository,
            FallOfWicketRepository fallOfWicketRepository) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.inningsRepository = inningsRepository;
        this.battingScoreRepository = battingScoreRepository;
        this.bowlingScoreRepository = bowlingScoreRepository;
        this.fallOfWicketRepository = fallOfWicketRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchSummaryDto> getAllMatches(MatchFormat format) {
        List<CricketMatch> matches;
        if (format != null) {
            matches = matchRepository.findByFormatOrderByMatchDateDesc(format);
        } else {
            matches = matchRepository.findAllByOrderByMatchDateDesc();
        }
        return matches.stream().map(this::mapToSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchSummaryDto> getLiveMatches() {
        List<CricketMatch> matches = matchRepository.findByStatusInOrderByMatchDateDesc(
                List.of(MatchStatus.LIVE, MatchStatus.INNINGS_BREAK)
        );
        return matches.stream().map(this::mapToSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchSummaryDto> getUpcomingMatches() {
        return matchRepository.findByStatusOrderByMatchDateDesc(MatchStatus.SCHEDULED)
                .stream().map(this::mapToSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchSummaryDto> getTodayMatches() {
        return matchRepository.findAllByOrderByMatchDateDesc().stream()
                .filter(m -> m.getMatchDate() != null && m.getMatchDate().toLocalDate().isEqual(java.time.LocalDate.now()))
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchSummaryDto> getTomorrowMatches() {
        return matchRepository.findAllByOrderByMatchDateDesc().stream()
                .filter(m -> m.getMatchDate() != null && m.getMatchDate().toLocalDate().isEqual(java.time.LocalDate.now().plusDays(1)))
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchSummaryDto> getCompletedMatches(MatchFormat format) {
        List<CricketMatch> matches = matchRepository.findByStatusOrderByMatchDateDesc(MatchStatus.COMPLETED);
        if (format != null) {
            matches = matches.stream().filter(m -> m.getFormat() == format).collect(Collectors.toList());
        }
        return matches.stream().map(this::mapToSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MatchSummaryDto getMatchSummary(Long matchId) {
        CricketMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));
        return mapToSummary(match);
    }

    @Transactional(readOnly = true)
    public ScorecardResponseDto getScorecard(Long matchId) {
        CricketMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));

        ScorecardResponseDto dto = new ScorecardResponseDto();
        dto.setMatchId(match.getId());
        dto.setTitle(match.getTitle());
        dto.setVenue(match.getVenue());
        dto.setFormat(match.getFormat());
        dto.setStatus(match.getStatus());
        dto.setResultMargin(match.getResultMargin());
        dto.setMatchSummary(match.getMatchSummary());
        if (match.getPlayerOfTheMatch() != null) {
            dto.setPlayerOfTheMatchName(match.getPlayerOfTheMatch().getName());
        }
        if (match.getTossWinner() != null) {
            dto.setTossInfo(match.getTossWinner().getName() + " won toss & chose to " +
                    (match.getTossDecision() != null ? match.getTossDecision().name().toLowerCase() : "bat"));
        }

        List<Innings> inningsList = inningsRepository.findByMatchIdOrderByInningsNumberAsc(matchId);
        List<ScorecardResponseDto.InningsScorecardDto> inningsDtos = new ArrayList<>();

        for (Innings inn : inningsList) {
            ScorecardResponseDto.InningsScorecardDto innDto = new ScorecardResponseDto.InningsScorecardDto();
            innDto.setInningsId(inn.getId());
            innDto.setInningsNumber(inn.getInningsNumber());
            innDto.setBattingTeamName(inn.getBattingTeam().getName());
            innDto.setBattingTeamShortName(inn.getBattingTeam().getShortName());
            innDto.setBowlingTeamName(inn.getBowlingTeam().getName());
            innDto.setBowlingTeamShortName(inn.getBowlingTeam().getShortName());
            innDto.setTotalRuns(inn.getTotalRuns());
            innDto.setWickets(inn.getWickets());
            innDto.setOversFormatted(inn.getOversFormatted());
            innDto.setRunRate(inn.getCurrentRunRate());

            innDto.setExtrasTotal(inn.getTotalExtras());
            innDto.setExtrasWide(inn.getExtrasWide());
            innDto.setExtrasNoBall(inn.getExtrasNoBall());
            innDto.setExtrasBye(inn.getExtrasBye());
            innDto.setExtrasLegBye(inn.getExtrasLegBye());

            // Batting cards
            List<BattingScore> battingScores = battingScoreRepository.findByInningsIdOrderByBattingOrderAsc(inn.getId());
            innDto.setBattingCards(battingScores.stream().map(bs -> new ScorecardResponseDto.BattingCardRowDto(
                    bs.getPlayer().getId(),
                    bs.getPlayer().getName(),
                    bs.getDismissalText(),
                    bs.getRuns(),
                    bs.getBallsFaced(),
                    bs.getFours(),
                    bs.getSixes(),
                    bs.getStrikeRate(),
                    bs.getIsOut()
            )).collect(Collectors.toList()));

            // Did Not Bat (players in batting team who didn't bat)
            List<Player> allTeamPlayers = playerRepository.findByTeamIdOrderByNameAsc(inn.getBattingTeam().getId());
            List<Long> battedPlayerIds = battingScores.stream().map(bs -> bs.getPlayer().getId()).toList();
            List<String> didNotBat = allTeamPlayers.stream()
                    .filter(p -> !battedPlayerIds.contains(p.getId()))
                    .map(Player::getName)
                    .collect(Collectors.toList());
            innDto.setDidNotBat(didNotBat);

            // Bowling cards
            List<BowlingScore> bowlingScores = bowlingScoreRepository.findByInningsIdOrderByIdAsc(inn.getId());
            innDto.setBowlingCards(bowlingScores.stream().map(bs -> new ScorecardResponseDto.BowlingCardRowDto(
                    bs.getPlayer().getId(),
                    bs.getPlayer().getName(),
                    bs.getOversFormatted(),
                    bs.getMaidens(),
                    bs.getRunsConceded(),
                    bs.getWickets(),
                    bs.getEconomy()
            )).collect(Collectors.toList()));

            // Fall of wickets
            List<FallOfWicket> fows = fallOfWicketRepository.findByInningsIdOrderByWicketNumberAsc(inn.getId());
            innDto.setFallOfWickets(fows.stream().map(f -> new ScorecardResponseDto.FallOfWicketDto(
                    f.getWicketNumber(),
                    f.getTeamScore(),
                    f.getOversFormatted(),
                    f.getDismissedPlayer().getName()
            )).collect(Collectors.toList()));

            inningsDtos.add(innDto);
        }

        dto.setInnings(inningsDtos);
        return dto;
    }

    @Transactional
    public CricketMatch createMatch(CreateMatchRequestDto request) {
        if (Objects.equals(request.getTeam1Id(), request.getTeam2Id())) {
            throw new BadRequestException("Team 1 and Team 2 must be different teams.");
        }

        Team team1 = teamRepository.findById(request.getTeam1Id())
                .orElseThrow(() -> new ResourceNotFoundException("Team 1 not found: " + request.getTeam1Id()));
        Team team2 = teamRepository.findById(request.getTeam2Id())
                .orElseThrow(() -> new ResourceNotFoundException("Team 2 not found: " + request.getTeam2Id()));

        CricketMatch match = new CricketMatch();
        match.setTitle(request.getTitle());
        match.setVenue(request.getVenue());
        match.setMatchDate(request.getMatchDate() != null ? request.getMatchDate() : java.time.LocalDateTime.now());
        match.setFormat(request.getFormat() != null ? request.getFormat() : MatchFormat.T20);
        match.setTotalOvers(request.getTotalOvers() != null ? request.getTotalOvers() : 20);
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setStatus(MatchStatus.SCHEDULED);

        if (request.getTossWinnerId() != null) {
            Team tossWinner = Objects.equals(request.getTossWinnerId(), team1.getId()) ? team1 : team2;
            match.setTossWinner(tossWinner);
            match.setTossDecision(request.getTossDecision());
        }

        CricketMatch savedMatch = matchRepository.save(match);

        // Determine batting team for 1st innings based on toss
        Team battingTeam1 = team1;
        Team bowlingTeam1 = team2;
        if (match.getTossWinner() != null && match.getTossDecision() != null) {
            if (match.getTossDecision() == TossDecision.BAT) {
                battingTeam1 = match.getTossWinner();
                bowlingTeam1 = Objects.equals(match.getTossWinner().getId(), team1.getId()) ? team2 : team1;
            } else {
                bowlingTeam1 = match.getTossWinner();
                battingTeam1 = Objects.equals(match.getTossWinner().getId(), team1.getId()) ? team2 : team1;
            }
        }

        // Initialize 1st innings
        Innings inn1 = new Innings(savedMatch, 1, battingTeam1, bowlingTeam1);
        inningsRepository.save(inn1);

        return savedMatch;
    }

    public MatchSummaryDto mapToSummary(CricketMatch match) {
        MatchSummaryDto dto = new MatchSummaryDto();
        dto.setId(match.getId());
        dto.setTitle(match.getTitle());
        dto.setVenue(match.getVenue());
        dto.setMatchDate(match.getMatchDate());
        dto.setFormat(match.getFormat());
        dto.setTotalOvers(match.getTotalOvers());
        dto.setStatus(match.getStatus());

        dto.setTeam1Id(match.getTeam1().getId());
        dto.setTeam1Name(match.getTeam1().getName());
        dto.setTeam1ShortName(match.getTeam1().getShortName());
        dto.setTeam1Color(match.getTeam1().getPrimaryColor());

        dto.setTeam2Id(match.getTeam2().getId());
        dto.setTeam2Name(match.getTeam2().getName());
        dto.setTeam2ShortName(match.getTeam2().getShortName());
        dto.setTeam2Color(match.getTeam2().getPrimaryColor());

        dto.setResultMargin(match.getResultMargin());
        dto.setSeriesName(match.getSeriesName());
        dto.setDayNote(match.getDayNote());

        if (match.getMatchDate() != null) {
            java.time.LocalDate matchDateOnly = match.getMatchDate().toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now();
            boolean isToday = matchDateOnly.isEqual(today);
            boolean isTomorrow = matchDateOnly.isEqual(today.plusDays(1));
            dto.setToday(isToday);
            dto.setTomorrow(isTomorrow);

            if (isToday) {
                dto.setDayLabel("TODAY");
            } else if (isTomorrow) {
                dto.setDayLabel("TOMORROW");
            } else if (matchDateOnly.isEqual(today.minusDays(1))) {
                dto.setDayLabel("YESTERDAY");
            } else {
                dto.setDayLabel(match.getMatchDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM")));
            }
            dto.setMatchTime(match.getMatchDate().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")));
        }

        List<Innings> inningsList = inningsRepository.findByMatchIdOrderByInningsNumberAsc(match.getId());
        if (!inningsList.isEmpty()) {
            Innings inn1 = inningsList.get(0);
            dto.setInnings1Runs(inn1.getTotalRuns());
            dto.setInnings1Wickets(inn1.getWickets());
            dto.setInnings1Overs(inn1.getOversFormatted());
            dto.setInnings1TeamName(inn1.getBattingTeam().getShortName());

            if (inningsList.size() > 1) {
                Innings inn2 = inningsList.get(1);
                dto.setInnings2Runs(inn2.getTotalRuns());
                dto.setInnings2Wickets(inn2.getWickets());
                dto.setInnings2Overs(inn2.getOversFormatted());
                dto.setInnings2TeamName(inn2.getBattingTeam().getShortName());
                dto.setTargetRuns(inn2.getTargetRuns());
                dto.setCurrentRunRate(inn2.getCurrentRunRate());
                dto.setRequiredRunRate(inn2.getRequiredRunRate());

                if (match.getStatus() == MatchStatus.LIVE) {
                    if (match.getDayNote() != null && !match.getDayNote().isBlank()) {
                        dto.setCurrentStatusNote(match.getDayNote());
                    } else if (inn2.getTargetRuns() != null) {
                        int needed = inn2.getTargetRuns() - inn2.getTotalRuns();
                        int maxBalls = match.getTotalOvers() * 6;
                        int ballsLeft = maxBalls - inn2.getLegalBalls();
                        dto.setCurrentStatusNote(inn2.getBattingTeam().getShortName() + " need " + needed + " runs in " + ballsLeft + " balls");
                    }
                }
            } else {
                dto.setCurrentRunRate(inn1.getCurrentRunRate());
                if (match.getStatus() == MatchStatus.LIVE) {
                    if (match.getDayNote() != null && !match.getDayNote().isBlank()) {
                        dto.setCurrentStatusNote(match.getDayNote());
                    } else {
                        dto.setCurrentStatusNote(inn1.getBattingTeam().getShortName() + " batting first (" + inn1.getOversFormatted() + " ov)");
                    }
                }
            }
        } else {
            if (match.getStatus() == MatchStatus.SCHEDULED) {
                dto.setCurrentStatusNote(match.getDayNote() != null ? match.getDayNote() : ("Scheduled at " + dto.getMatchTime()));
            }
        }

        if (match.getStatus() == MatchStatus.INNINGS_BREAK) {
            dto.setCurrentStatusNote(match.getDayNote() != null ? match.getDayNote() : "Innings Break");
        } else if (match.getStatus() == MatchStatus.COMPLETED) {
            dto.setCurrentStatusNote(match.getResultMargin() != null ? match.getResultMargin() : "Match Finished");
        }

        return dto;
    }
}
