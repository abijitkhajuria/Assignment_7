package com.cricpulse.service;

import com.cricpulse.dto.DeliveryRequestDto;
import com.cricpulse.entity.*;
import com.cricpulse.exception.BadRequestException;
import com.cricpulse.exception.InvalidScoringException;
import com.cricpulse.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ScoringEngineServiceTest {

    @Autowired
    private ScoringEngineService scoringEngineService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private InningsRepository inningsRepository;

    @Autowired
    private BattingScoreRepository battingScoreRepository;

    @Autowired
    private BowlingScoreRepository bowlingScoreRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private FallOfWicketRepository fallOfWicketRepository;

    private Team teamIndia;
    private Team teamAustralia;
    private Player striker;
    private Player nonStriker;
    private Player newBatter;
    private Player bowler;
    private CricketMatch match;
    private Innings innings;

    @BeforeEach
    void setUp() {
        teamIndia = teamRepository.save(new Team("Test India", "T-IND", null, "#0077B6"));
        teamAustralia = teamRepository.save(new Team("Test Australia", "T-AUS", null, "#F59E0B"));

        striker = playerRepository.save(new Player("Striker Rohit", teamIndia, PlayerRole.BATSMAN, "RHB", "None", 45));
        nonStriker = playerRepository.save(new Player("NonStriker Virat", teamIndia, PlayerRole.BATSMAN, "RHB", "None", 18));
        newBatter = playerRepository.save(new Player("Next Surya", teamIndia, PlayerRole.BATSMAN, "RHB", "None", 63));
        bowler = playerRepository.save(new Player("Bowler Starc", teamAustralia, PlayerRole.BOWLER, "LHB", "Fast", 56));

        match = new CricketMatch();
        match.setTitle("Test Final Match");
        match.setVenue("Test Ground");
        match.setMatchDate(LocalDateTime.now());
        match.setFormat(MatchFormat.T20);
        match.setTotalOvers(20);
        match.setTeam1(teamIndia);
        match.setTeam2(teamAustralia);
        match.setStatus(MatchStatus.LIVE);
        match = matchRepository.save(match);

        innings = new Innings(match, 1, teamIndia, teamAustralia);
        innings.setCurrentStriker(striker);
        innings.setCurrentNonStriker(nonStriker);
        innings.setCurrentBowler(bowler);
        innings = inningsRepository.save(innings);
    }

    @Test
    @DisplayName("1. Dot ball: 0 runs, 1 legal delivery, batter balls faced increments")
    void testDotBall() {
        DeliveryRequestDto req = new DeliveryRequestDto();
        req.setStrikerId(striker.getId());
        req.setNonStrikerId(nonStriker.getId());
        req.setBowlerId(bowler.getId());
        req.setRunsBatter(0);
        req.setExtraType(ExtraType.NONE);

        Delivery delivery = scoringEngineService.recordDelivery(innings.getId(), req);

        Innings updated = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(0, updated.getTotalRuns());
        assertEquals(1, updated.getLegalBalls());
        assertEquals(0, updated.getWickets());
        assertTrue(delivery.getIsLegalDelivery());

        BattingScore bs = battingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), striker.getId()).orElseThrow();
        assertEquals(0, bs.getRuns());
        assertEquals(1, bs.getBallsFaced());
    }

    @Test
    @DisplayName("2. Four and Six: boundary tracking for batter and bowler runs conceded")
    void testFourAndSix() {
        // First ball: FOUR
        DeliveryRequestDto req4 = new DeliveryRequestDto();
        req4.setStrikerId(striker.getId());
        req4.setNonStrikerId(nonStriker.getId());
        req4.setBowlerId(bowler.getId());
        req4.setRunsBatter(4);
        scoringEngineService.recordDelivery(innings.getId(), req4);

        // Second ball: SIX
        DeliveryRequestDto req6 = new DeliveryRequestDto();
        req6.setStrikerId(striker.getId());
        req6.setNonStrikerId(nonStriker.getId());
        req6.setBowlerId(bowler.getId());
        req6.setRunsBatter(6);
        scoringEngineService.recordDelivery(innings.getId(), req6);

        Innings updated = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(10, updated.getTotalRuns());
        assertEquals(2, updated.getLegalBalls());

        BattingScore bs = battingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), striker.getId()).orElseThrow();
        assertEquals(10, bs.getRuns());
        assertEquals(2, bs.getBallsFaced());
        assertEquals(1, bs.getFours());
        assertEquals(1, bs.getSixes());
        assertEquals(500.0, bs.getStrikeRate());

        BowlingScore bowScore = bowlingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), bowler.getId()).orElseThrow();
        assertEquals(10, bowScore.getRunsConceded());
        assertEquals(2, bowScore.getLegalBallsBowled());
    }

    @Test
    @DisplayName("3. Wide and No-Ball: Illegal deliveries do NOT increment legal balls")
    void testWideAndNoBall() {
        // Wide ball: 1 extra run, 0 legal balls, batter balls faced does NOT increase
        DeliveryRequestDto wideReq = new DeliveryRequestDto();
        wideReq.setStrikerId(striker.getId());
        wideReq.setNonStrikerId(nonStriker.getId());
        wideReq.setBowlerId(bowler.getId());
        wideReq.setExtraType(ExtraType.WIDE);
        wideReq.setRunsExtra(1);
        Delivery wideDelivery = scoringEngineService.recordDelivery(innings.getId(), wideReq);

        assertFalse(wideDelivery.getIsLegalDelivery());
        Innings afterWide = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(1, afterWide.getTotalRuns());
        assertEquals(1, afterWide.getExtrasWide());
        assertEquals(0, afterWide.getLegalBalls());

        // No-ball with 2 batter runs: 1 extra + 2 batter = 3 runs total, legal balls still 0
        DeliveryRequestDto nbReq = new DeliveryRequestDto();
        nbReq.setStrikerId(striker.getId());
        nbReq.setNonStrikerId(nonStriker.getId());
        nbReq.setBowlerId(bowler.getId());
        nbReq.setExtraType(ExtraType.NO_BALL);
        nbReq.setRunsExtra(1);
        nbReq.setRunsBatter(2);
        Delivery nbDelivery = scoringEngineService.recordDelivery(innings.getId(), nbReq);

        assertFalse(nbDelivery.getIsLegalDelivery());
        Innings afterNb = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(4, afterNb.getTotalRuns()); // 1 + 3 = 4
        assertEquals(1, afterNb.getExtrasNoBall());
        assertEquals(0, afterNb.getLegalBalls());

        BattingScore bs = battingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), striker.getId()).orElseThrow();
        assertEquals(2, bs.getRuns());
        assertEquals(1, bs.getBallsFaced()); // Batter faced the no-ball, but not the wide
    }

    @Test
    @DisplayName("4. Bye and Leg-Bye: Legal deliveries, NOT charged to bowler runs")
    void testByeAndLegBye() {
        // 2 Byes
        DeliveryRequestDto byeReq = new DeliveryRequestDto();
        byeReq.setStrikerId(striker.getId());
        byeReq.setNonStrikerId(nonStriker.getId());
        byeReq.setBowlerId(bowler.getId());
        byeReq.setExtraType(ExtraType.BYE);
        byeReq.setRunsExtra(2);
        scoringEngineService.recordDelivery(innings.getId(), byeReq);

        Innings updated = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(2, updated.getTotalRuns());
        assertEquals(1, updated.getLegalBalls());
        assertEquals(2, updated.getExtrasBye());

        BowlingScore bowScore = bowlingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), bowler.getId()).orElseThrow();
        assertEquals(0, bowScore.getRunsConceded()); // Byes not charged to bowler!
        assertEquals(1, bowScore.getLegalBallsBowled());
    }

    @Test
    @DisplayName("5. Wicket & Run Out: Bowler credited on bowled/caught, NOT on run out")
    void testWickets() {
        // Bowled wicket: bowler gets credit
        DeliveryRequestDto bowledReq = new DeliveryRequestDto();
        bowledReq.setStrikerId(striker.getId());
        bowledReq.setNonStrikerId(nonStriker.getId());
        bowledReq.setBowlerId(bowler.getId());
        bowledReq.setIsWicket(true);
        bowledReq.setWicketType(WicketType.BOWLED);
        bowledReq.setNewBatsmanId(newBatter.getId());
        scoringEngineService.recordDelivery(innings.getId(), bowledReq);

        Innings updated = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(1, updated.getWickets());

        BattingScore rohitScore = battingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), striker.getId()).orElseThrow();
        assertTrue(rohitScore.getIsOut());
        assertEquals("b Bowler Starc", rohitScore.getDismissalText());

        BowlingScore starcScore = bowlingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), bowler.getId()).orElseThrow();
        assertEquals(1, starcScore.getWickets()); // Credited to bowler

        // Run out wicket: NOT credited to bowler
        DeliveryRequestDto runOutReq = new DeliveryRequestDto();
        runOutReq.setStrikerId(newBatter.getId());
        runOutReq.setNonStrikerId(nonStriker.getId());
        runOutReq.setBowlerId(bowler.getId());
        runOutReq.setIsWicket(true);
        runOutReq.setWicketType(WicketType.RUN_OUT);
        scoringEngineService.recordDelivery(innings.getId(), runOutReq);

        Innings afterRunout = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(2, afterRunout.getWickets());

        BowlingScore starcScoreAfterRunout = bowlingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), bowler.getId()).orElseThrow();
        assertEquals(1, starcScoreAfterRunout.getWickets()); // Stays 1, not credited with run out!
    }

    @Test
    @DisplayName("6. Strike Rotation: Odd runs swap striker and non-striker")
    void testStrikeRotationOnSingle() {
        DeliveryRequestDto singleReq = new DeliveryRequestDto();
        singleReq.setStrikerId(striker.getId());
        singleReq.setNonStrikerId(nonStriker.getId());
        singleReq.setBowlerId(bowler.getId());
        singleReq.setRunsBatter(1);

        scoringEngineService.recordDelivery(innings.getId(), singleReq);

        Innings updated = inningsRepository.findById(innings.getId()).orElseThrow();
        // Since 1 run was taken, nonStriker (Virat) becomes current striker, striker (Rohit) becomes nonStriker
        assertEquals(nonStriker.getId(), updated.getCurrentStriker().getId());
        assertEquals(striker.getId(), updated.getCurrentNonStriker().getId());
    }

    @Test
    @DisplayName("7. Over Completion: 6th ball triggers end-of-over strike rotation")
    void testOverCompletion() {
        // Bowl 5 dot balls
        for (int i = 0; i < 5; i++) {
            DeliveryRequestDto dot = new DeliveryRequestDto();
            dot.setStrikerId(striker.getId());
            dot.setNonStrikerId(nonStriker.getId());
            dot.setBowlerId(bowler.getId());
            dot.setRunsBatter(0);
            scoringEngineService.recordDelivery(innings.getId(), dot);
        }

        Innings after5 = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(5, after5.getLegalBalls());
        assertEquals(striker.getId(), after5.getCurrentStriker().getId());

        // 6th ball: 0 runs -> Over complete! End of over swaps ends so nonStriker becomes striker!
        DeliveryRequestDto ball6 = new DeliveryRequestDto();
        ball6.setStrikerId(striker.getId());
        ball6.setNonStrikerId(nonStriker.getId());
        ball6.setBowlerId(bowler.getId());
        ball6.setRunsBatter(0);
        scoringEngineService.recordDelivery(innings.getId(), ball6);

        Innings after6 = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(6, after6.getLegalBalls());
        assertEquals("1.0", after6.getOversFormatted());
        assertEquals(nonStriker.getId(), after6.getCurrentStriker().getId());
        assertEquals(striker.getId(), after6.getCurrentNonStriker().getId());
    }

    @Test
    @DisplayName("8. Chase Result: Reaching target completes match with victory margin")
    void testChaseResult() {
        // Set up 2nd innings chasing 10 runs to win
        Innings inn2 = new Innings(match, 2, teamAustralia, teamIndia);
        inn2.setTargetRuns(10);
        inn2.setCurrentStriker(bowler);
        inn2.setCurrentNonStriker(newBatter);
        inn2.setCurrentBowler(striker);
        inn2 = inningsRepository.save(inn2);

        // Australia hits a SIX: 6 runs
        DeliveryRequestDto d1 = new DeliveryRequestDto();
        d1.setStrikerId(bowler.getId());
        d1.setNonStrikerId(newBatter.getId());
        d1.setBowlerId(striker.getId());
        d1.setRunsBatter(6);
        scoringEngineService.recordDelivery(inn2.getId(), d1);

        // Australia hits a FOUR: 10 runs >= 10 target -> Won by 10 wickets!
        DeliveryRequestDto d2 = new DeliveryRequestDto();
        d2.setStrikerId(bowler.getId());
        d2.setNonStrikerId(newBatter.getId());
        d2.setBowlerId(striker.getId());
        d2.setRunsBatter(4);
        scoringEngineService.recordDelivery(inn2.getId(), d2);

        Innings completedInn = inningsRepository.findById(inn2.getId()).orElseThrow();
        assertTrue(completedInn.getIsCompleted());

        CricketMatch completedMatch = matchRepository.findById(match.getId()).orElseThrow();
        assertEquals(MatchStatus.COMPLETED, completedMatch.getStatus());
        assertEquals(teamAustralia.getId(), completedMatch.getWinnerTeam().getId());
        assertTrue(completedMatch.getResultMargin().contains("won by 10 wickets"));
    }

    @Test
    @DisplayName("9. Undo Last Delivery: Reverts score, wickets, batter/bowler figures")
    void testUndoLastDelivery() {
        // Ball 1: hits a FOUR
        DeliveryRequestDto d1 = new DeliveryRequestDto();
        d1.setStrikerId(striker.getId());
        d1.setNonStrikerId(nonStriker.getId());
        d1.setBowlerId(bowler.getId());
        d1.setRunsBatter(4);
        scoringEngineService.recordDelivery(innings.getId(), d1);

        Innings beforeUndo = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(4, beforeUndo.getTotalRuns());
        assertEquals(1, beforeUndo.getLegalBalls());

        // Undo Ball 1
        scoringEngineService.undoLastDelivery(innings.getId());

        Innings afterUndo = inningsRepository.findById(innings.getId()).orElseThrow();
        assertEquals(0, afterUndo.getTotalRuns());
        assertEquals(0, afterUndo.getLegalBalls());

        BattingScore bs = battingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), striker.getId()).orElseThrow();
        assertEquals(0, bs.getRuns());
        assertEquals(0, bs.getBallsFaced());
        assertEquals(0, bs.getFours());
    }

    @Test
    @DisplayName("10. Invalid Scoring: Rejects same striker/non-striker or bowler as batter")
    void testInvalidScoringInputs() {
        DeliveryRequestDto invalidSame = new DeliveryRequestDto();
        invalidSame.setStrikerId(striker.getId());
        invalidSame.setNonStrikerId(striker.getId()); // Same!
        invalidSame.setBowlerId(bowler.getId());

        assertThrows(BadRequestException.class, () ->
                scoringEngineService.recordDelivery(innings.getId(), invalidSame)
        );

        DeliveryRequestDto invalidBowler = new DeliveryRequestDto();
        invalidBowler.setStrikerId(striker.getId());
        invalidBowler.setNonStrikerId(nonStriker.getId());
        invalidBowler.setBowlerId(striker.getId()); // Bowler is striker!

        assertThrows(BadRequestException.class, () ->
                scoringEngineService.recordDelivery(innings.getId(), invalidBowler)
        );
    }
}
