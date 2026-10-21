package com.cricpulse.service;

import com.cricpulse.dto.DeliveryRequestDto;
import com.cricpulse.dto.LiveMatchSnapshotDto;
import com.cricpulse.entity.*;
import com.cricpulse.exception.BadRequestException;
import com.cricpulse.exception.InvalidScoringException;
import com.cricpulse.exception.ResourceNotFoundException;
import com.cricpulse.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ScoringEngineService {

    private final InningsRepository inningsRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final DeliveryRepository deliveryRepository;
    private final BattingScoreRepository battingScoreRepository;
    private final BowlingScoreRepository bowlingScoreRepository;
    private final FallOfWicketRepository fallOfWicketRepository;
    private final SseBroadcastService sseBroadcastService;

    public ScoringEngineService(
            InningsRepository inningsRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            DeliveryRepository deliveryRepository,
            BattingScoreRepository battingScoreRepository,
            BowlingScoreRepository bowlingScoreRepository,
            FallOfWicketRepository fallOfWicketRepository,
            SseBroadcastService sseBroadcastService) {
        this.inningsRepository = inningsRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.deliveryRepository = deliveryRepository;
        this.battingScoreRepository = battingScoreRepository;
        this.bowlingScoreRepository = bowlingScoreRepository;
        this.fallOfWicketRepository = fallOfWicketRepository;
        this.sseBroadcastService = sseBroadcastService;
    }

    @Transactional
    public Delivery recordDelivery(Long inningsId, DeliveryRequestDto request) {
        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() -> new ResourceNotFoundException("Innings not found with ID: " + inningsId));

        CricketMatch match = innings.getMatch();

        if (Boolean.TRUE.equals(innings.getIsCompleted())) {
            throw new InvalidScoringException("Cannot score: This innings is already marked as completed.");
        }
        if (match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.ABANDONED) {
            throw new InvalidScoringException("Cannot score: Match status is " + match.getStatus());
        }

        // Validate players
        if (Objects.equals(request.getStrikerId(), request.getNonStrikerId())) {
            throw new BadRequestException("Striker and non-striker cannot be the same player.");
        }
        if (Objects.equals(request.getBowlerId(), request.getStrikerId()) ||
            Objects.equals(request.getBowlerId(), request.getNonStrikerId())) {
            throw new BadRequestException("Bowler cannot be currently batting at the crease.");
        }

        Player striker = playerRepository.findById(request.getStrikerId())
                .orElseThrow(() -> new ResourceNotFoundException("Striker not found: " + request.getStrikerId()));
        Player nonStriker = playerRepository.findById(request.getNonStrikerId())
                .orElseThrow(() -> new ResourceNotFoundException("Non-striker not found: " + request.getNonStrikerId()));
        Player bowler = playerRepository.findById(request.getBowlerId())
                .orElseThrow(() -> new ResourceNotFoundException("Bowler not found: " + request.getBowlerId()));

        // Ensure match is marked LIVE
        if (match.getStatus() == MatchStatus.SCHEDULED || match.getStatus() == MatchStatus.INNINGS_BREAK) {
            match.setStatus(MatchStatus.LIVE);
            matchRepository.save(match);
        }

        // Get or initialize BattingScore for striker & non-striker
        BattingScore strikerScore = getOrCreateBattingScore(innings, striker);
        BattingScore nonStrikerScore = getOrCreateBattingScore(innings, nonStriker);

        // Get or initialize BowlingScore for bowler
        BowlingScore bowlerScore = getOrCreateBowlingScore(innings, bowler);

        // Determine runs & legal delivery status
        ExtraType extraType = request.getExtraType() != null ? request.getExtraType() : ExtraType.NONE;
        int batterRuns = request.getRunsBatter() != null ? request.getRunsBatter() : 0;
        int extraRuns = request.getRunsExtra() != null ? request.getRunsExtra() : 0;
        boolean isLegal = true;
        int bowlerConcededRuns = 0;
        int inningsAddedRuns = 0;
        int runsForStrikeRotation = 0;

        switch (extraType) {
            case WIDE:
                isLegal = false;
                extraRuns = extraRuns > 0 ? extraRuns : 1;
                inningsAddedRuns = extraRuns + batterRuns;
                bowlerConcededRuns = inningsAddedRuns;
                innings.setExtrasWide(innings.getExtrasWide() + extraRuns);
                runsForStrikeRotation = batterRuns; // Byes/runs taken on wide
                break;

            case NO_BALL:
                isLegal = false;
                extraRuns = extraRuns > 0 ? extraRuns : 1;
                inningsAddedRuns = extraRuns + batterRuns;
                bowlerConcededRuns = inningsAddedRuns;
                innings.setExtrasNoBall(innings.getExtrasNoBall() + extraRuns);
                // Batter faces delivery and gets credited runs on no ball
                strikerScore.setBallsFaced(strikerScore.getBallsFaced() + 1);
                strikerScore.setRuns(strikerScore.getRuns() + batterRuns);
                if (batterRuns == 4) strikerScore.setFours(strikerScore.getFours() + 1);
                if (batterRuns == 6) strikerScore.setSixes(strikerScore.getSixes() + 1);
                runsForStrikeRotation = batterRuns;
                break;

            case BYE:
                isLegal = true;
                extraRuns = extraRuns > 0 ? extraRuns : (batterRuns > 0 ? batterRuns : 1);
                batterRuns = 0; // Batter gets no personal runs on byes
                inningsAddedRuns = extraRuns;
                bowlerConcededRuns = 0; // NOT charged to bowler
                innings.setExtrasBye(innings.getExtrasBye() + extraRuns);
                strikerScore.setBallsFaced(strikerScore.getBallsFaced() + 1);
                runsForStrikeRotation = extraRuns;
                break;

            case LEG_BYE:
                isLegal = true;
                extraRuns = extraRuns > 0 ? extraRuns : (batterRuns > 0 ? batterRuns : 1);
                batterRuns = 0; // Batter gets no personal runs on leg byes
                inningsAddedRuns = extraRuns;
                bowlerConcededRuns = 0; // NOT charged to bowler
                innings.setExtrasLegBye(innings.getExtrasLegBye() + extraRuns);
                strikerScore.setBallsFaced(strikerScore.getBallsFaced() + 1);
                runsForStrikeRotation = extraRuns;
                break;

            case PENALTY:
                isLegal = false;
                extraRuns = extraRuns > 0 ? extraRuns : 5;
                inningsAddedRuns = extraRuns;
                bowlerConcededRuns = 0;
                innings.setExtrasPenalty(innings.getExtrasPenalty() + extraRuns);
                runsForStrikeRotation = 0;
                break;

            case NONE:
            default:
                isLegal = true;
                extraRuns = 0;
                inningsAddedRuns = batterRuns;
                bowlerConcededRuns = batterRuns;
                strikerScore.setBallsFaced(strikerScore.getBallsFaced() + 1);
                strikerScore.setRuns(strikerScore.getRuns() + batterRuns);
                if (batterRuns == 4) strikerScore.setFours(strikerScore.getFours() + 1);
                if (batterRuns == 6) strikerScore.setSixes(strikerScore.getSixes() + 1);
                runsForStrikeRotation = batterRuns;
                break;
        }

        // Update innings totals
        innings.setTotalRuns(innings.getTotalRuns() + inningsAddedRuns);
        if (isLegal) {
            innings.setLegalBalls(innings.getLegalBalls() + 1);
            bowlerScore.setLegalBallsBowled(bowlerScore.getLegalBallsBowled() + 1);
        }
        bowlerScore.setRunsConceded(bowlerScore.getRunsConceded() + bowlerConcededRuns);

        // Sequence calculation
        long currentCount = deliveryRepository.countByInningsId(inningsId);
        int seq = (int) currentCount + 1;
        int currentOverNumber = (innings.getLegalBalls() - (isLegal ? 1 : 0)) / 6;
        int ballInOver = (innings.getLegalBalls() - (isLegal ? 1 : 0)) % 6 + (isLegal ? 1 : 0);

        // Create Delivery Record
        Delivery delivery = new Delivery();
        delivery.setInnings(innings);
        delivery.setOverNumber(currentOverNumber);
        delivery.setBallNumberInOver(ballInOver);
        delivery.setLegalBallNumber(innings.getLegalBalls());
        delivery.setStriker(striker);
        delivery.setNonStriker(nonStriker);
        delivery.setBowler(bowler);
        delivery.setRunsBatter(batterRuns);
        delivery.setRunsExtra(extraRuns);
        delivery.setExtraType(extraType);
        delivery.setIsLegalDelivery(isLegal);
        delivery.setSequenceOrder(seq);

        // Handle Wicket
        boolean isWicket = Boolean.TRUE.equals(request.getIsWicket());
        delivery.setIsWicket(isWicket);

        Player dismissedPlayer = null;
        Player fielder = null;

        if (isWicket) {
            WicketType wicketType = request.getWicketType() != null ? request.getWicketType() : WicketType.BOWLED;
            delivery.setWicketType(wicketType);

            // Determine dismissed player: default striker, or non-striker if specified (e.g. run out)
            dismissedPlayer = striker;
            if (request.getDismissedPlayerId() != null && Objects.equals(request.getDismissedPlayerId(), nonStriker.getId())) {
                dismissedPlayer = nonStriker;
            }
            delivery.setDismissedPlayer(dismissedPlayer);

            if (request.getFielderId() != null) {
                fielder = playerRepository.findById(request.getFielderId()).orElse(null);
                delivery.setFielder(fielder);
            }

            // Update innings wickets
            innings.setWickets(innings.getWickets() + 1);

            // Credit bowler if applicable
            if (wicketType != WicketType.RUN_OUT && wicketType != WicketType.RETIRED_HURT) {
                bowlerScore.setWickets(bowlerScore.getWickets() + 1);
            }

            // Dismiss the batsman
            BattingScore dismissedScore = Objects.equals(dismissedPlayer.getId(), striker.getId()) ? strikerScore : nonStrikerScore;
            dismissedScore.setIsOut(true);
            dismissedScore.setWicketType(wicketType);
            dismissedScore.setBowler(wicketType != WicketType.RUN_OUT ? bowler : null);
            dismissedScore.setFielder(fielder);
            dismissedScore.setDismissalText(formatDismissalText(wicketType, bowler, fielder));
            battingScoreRepository.save(dismissedScore);

            // Record Fall of Wicket
            FallOfWicket fow = new FallOfWicket(
                    innings,
                    innings.getWickets(),
                    innings.getTotalRuns(),
                    innings.getOversFormatted(),
                    dismissedPlayer
            );
            fallOfWicketRepository.save(fow);

            // Replace dismissed batsman if newBatsmanId is given
            if (request.getNewBatsmanId() != null && innings.getWickets() < 10) {
                Player newBatter = playerRepository.findById(request.getNewBatsmanId()).orElse(null);
                if (newBatter != null) {
                    getOrCreateBattingScore(innings, newBatter);
                    if (Objects.equals(dismissedPlayer.getId(), striker.getId())) {
                        striker = newBatter;
                    } else {
                        nonStriker = newBatter;
                    }
                }
            }
        }

        // Strike rotation logic
        // 1. Odd runs (1, 3, 5): batsmen cross
        if (runsForStrikeRotation % 2 != 0) {
            Player temp = striker;
            striker = nonStriker;
            nonStriker = temp;
        }

        // 2. End of Over strike rotation:
        // When legal ball reaches 6, 12, 18..., batsmen switch ends for the next over
        boolean isOverCompleted = isLegal && (innings.getLegalBalls() % 6 == 0);
        if (isOverCompleted) {
            Player temp = striker;
            striker = nonStriker;
            nonStriker = temp;
        }

        // Save active players at crease on innings
        innings.setCurrentStriker(striker);
        innings.setCurrentNonStriker(nonStriker);
        innings.setCurrentBowler(bowler);

        // Commentary generation
        String commentary = request.getCommentary();
        if (commentary == null || commentary.trim().isEmpty()) {
            commentary = buildAutomaticCommentary(bowler, striker, batterRuns, extraType, extraRuns, isWicket, delivery.getWicketType());
        }
        delivery.setCommentary(commentary);
        delivery.setTeamRunsAtBall(innings.getTotalRuns());
        delivery.setWicketsAtBall(innings.getWickets());
        delivery.setOversAtBallFormatted(innings.getOversFormatted());

        // Save all records
        battingScoreRepository.save(strikerScore);
        battingScoreRepository.save(nonStrikerScore);
        bowlingScoreRepository.save(bowlerScore);
        inningsRepository.save(innings);
        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Check Innings & Match Completion Conditions
        checkInningsAndMatchCompletion(match, innings);

        // Broadcast updated snapshot via SSE
        try {
            LiveMatchSnapshotDto snapshot = buildLiveMatchSnapshot(match.getId());
            sseBroadcastService.broadcast(match.getId(), snapshot);
        } catch (Exception e) {
            // Ignore broadcast failure during tests or when no clients connected
        }

        return savedDelivery;
    }

    @Transactional
    public void undoLastDelivery(Long inningsId) {
        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() -> new ResourceNotFoundException("Innings not found: " + inningsId));

        CricketMatch match = innings.getMatch();

        Optional<Delivery> lastDeliveryOpt = deliveryRepository.findTopByInningsIdOrderBySequenceOrderDesc(inningsId);
        if (lastDeliveryOpt.isEmpty()) {
            throw new BadRequestException("No deliveries found to undo in this innings.");
        }

        Delivery lastDelivery = lastDeliveryOpt.get();

        // 1. Revert Innings Runs & Extras
        int runsBatter = lastDelivery.getRunsBatter() != null ? lastDelivery.getRunsBatter() : 0;
        int runsExtra = lastDelivery.getRunsExtra() != null ? lastDelivery.getRunsExtra() : 0;
        int totalBallRuns = runsBatter + runsExtra;

        innings.setTotalRuns(Math.max(0, innings.getTotalRuns() - totalBallRuns));

        switch (lastDelivery.getExtraType()) {
            case WIDE:
                innings.setExtrasWide(Math.max(0, innings.getExtrasWide() - runsExtra));
                break;
            case NO_BALL:
                innings.setExtrasNoBall(Math.max(0, innings.getExtrasNoBall() - runsExtra));
                break;
            case BYE:
                innings.setExtrasBye(Math.max(0, innings.getExtrasBye() - runsExtra));
                break;
            case LEG_BYE:
                innings.setExtrasLegBye(Math.max(0, innings.getExtrasLegBye() - runsExtra));
                break;
            case PENALTY:
                innings.setExtrasPenalty(Math.max(0, innings.getExtrasPenalty() - runsExtra));
                break;
            default:
                break;
        }

        // 2. Revert Legal Balls
        if (Boolean.TRUE.equals(lastDelivery.getIsLegalDelivery())) {
            innings.setLegalBalls(Math.max(0, innings.getLegalBalls() - 1));
        }

        // 3. Revert Bowler Stats
        Optional<BowlingScore> bowlerScoreOpt = bowlingScoreRepository.findByInningsIdAndPlayerId(inningsId, lastDelivery.getBowler().getId());
        if (bowlerScoreOpt.isPresent()) {
            BowlingScore bs = bowlerScoreOpt.get();
            if (Boolean.TRUE.equals(lastDelivery.getIsLegalDelivery())) {
                bs.setLegalBallsBowled(Math.max(0, bs.getLegalBallsBowled() - 1));
            }
            // If wide or no-ball, bowler conceded runs = totalBallRuns; if bye/legbye, 0; if normal, runsBatter
            int bowlerRunsToDeduct = (lastDelivery.getExtraType() == ExtraType.BYE || lastDelivery.getExtraType() == ExtraType.LEG_BYE)
                    ? 0 : totalBallRuns;
            bs.setRunsConceded(Math.max(0, bs.getRunsConceded() - bowlerRunsToDeduct));

            if (Boolean.TRUE.equals(lastDelivery.getIsWicket()) &&
                lastDelivery.getWicketType() != WicketType.RUN_OUT &&
                lastDelivery.getWicketType() != WicketType.RETIRED_HURT) {
                bs.setWickets(Math.max(0, bs.getWickets() - 1));
            }
            bowlingScoreRepository.save(bs);
        }

        // 4. Revert Striker Stats
        Optional<BattingScore> strikerScoreOpt = battingScoreRepository.findByInningsIdAndPlayerId(inningsId, lastDelivery.getStriker().getId());
        if (strikerScoreOpt.isPresent()) {
            BattingScore bts = strikerScoreOpt.get();
            // Ball faced: only if legal or no-ball
            if (Boolean.TRUE.equals(lastDelivery.getIsLegalDelivery()) || lastDelivery.getExtraType() == ExtraType.NO_BALL) {
                bts.setBallsFaced(Math.max(0, bts.getBallsFaced() - 1));
            }
            bts.setRuns(Math.max(0, bts.getRuns() - runsBatter));
            if (runsBatter == 4) bts.setFours(Math.max(0, bts.getFours() - 1));
            if (runsBatter == 6) bts.setSixes(Math.max(0, bts.getSixes() - 1));
            battingScoreRepository.save(bts);
        }

        // 5. Revert Wicket & Fall of Wicket if applicable
        if (Boolean.TRUE.equals(lastDelivery.getIsWicket())) {
            innings.setWickets(Math.max(0, innings.getWickets() - 1));
            if (lastDelivery.getDismissedPlayer() != null) {
                Optional<BattingScore> dismissedScoreOpt = battingScoreRepository.findByInningsIdAndPlayerId(inningsId, lastDelivery.getDismissedPlayer().getId());
                if (dismissedScoreOpt.isPresent()) {
                    BattingScore ds = dismissedScoreOpt.get();
                    ds.setIsOut(false);
                    ds.setWicketType(WicketType.NONE);
                    ds.setBowler(null);
                    ds.setFielder(null);
                    ds.setDismissalText("not out");
                    battingScoreRepository.save(ds);
                }
            }
            fallOfWicketRepository.deleteByInningsIdAndWicketNumberGreaterThanEqual(inningsId, innings.getWickets() + 1);
        }

        // 6. Restore crease positions from the delivery
        innings.setCurrentStriker(lastDelivery.getStriker());
        innings.setCurrentNonStriker(lastDelivery.getNonStriker());
        innings.setCurrentBowler(lastDelivery.getBowler());

        // 7. Re-open Innings or Match if previously completed
        if (Boolean.TRUE.equals(innings.getIsCompleted())) {
            innings.setIsCompleted(false);
        }
        if (match.getStatus() == MatchStatus.COMPLETED || match.getStatus() == MatchStatus.INNINGS_BREAK) {
            match.setStatus(MatchStatus.LIVE);
            match.setResultMargin(null);
            match.setWinnerTeam(null);
            matchRepository.save(match);
        }

        inningsRepository.save(innings);

        // 8. Delete the delivery record
        deliveryRepository.delete(lastDelivery);

        // Broadcast updated snapshot via SSE
        try {
            LiveMatchSnapshotDto snapshot = buildLiveMatchSnapshot(match.getId());
            sseBroadcastService.broadcast(match.getId(), snapshot);
        } catch (Exception e) {
            // Ignore broadcast failure during testing
        }
    }

    @Transactional
    public void completeInnings(Long inningsId) {
        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() -> new ResourceNotFoundException("Innings not found: " + inningsId));
        CricketMatch match = innings.getMatch();

        innings.setIsCompleted(true);
        inningsRepository.save(innings);

        if (innings.getInningsNumber() == 1) {
            match.setStatus(MatchStatus.INNINGS_BREAK);
            // Create or update 2nd innings with target
            Optional<Innings> innings2Opt = inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 2);
            Innings innings2;
            if (innings2Opt.isPresent()) {
                innings2 = innings2Opt.get();
                innings2.setTargetRuns(innings.getTotalRuns() + 1);
            } else {
                innings2 = new Innings(match, 2, innings.getBowlingTeam(), innings.getBattingTeam());
                innings2.setTargetRuns(innings.getTotalRuns() + 1);
            }
            inningsRepository.save(innings2);
            match.setCurrentInningsNumber(2);
            matchRepository.save(match);
        } else {
            finalizeMatchResult(match, innings);
        }

        try {
            LiveMatchSnapshotDto snapshot = buildLiveMatchSnapshot(match.getId());
            sseBroadcastService.broadcast(match.getId(), snapshot);
        } catch (Exception ignored) {}
    }

    private void checkInningsAndMatchCompletion(CricketMatch match, Innings innings) {
        int maxLegalBalls = match.getTotalOvers() * 6;
        boolean allOut = innings.getWickets() >= 10;
        boolean oversFinished = innings.getLegalBalls() >= maxLegalBalls;

        if (innings.getInningsNumber() == 2) {
            // Target chase check: batting team reaches or surpasses target
            if (innings.getTargetRuns() != null && innings.getTotalRuns() >= innings.getTargetRuns()) {
                innings.setIsCompleted(true);
                inningsRepository.save(innings);

                match.setStatus(MatchStatus.COMPLETED);
                match.setWinnerTeam(innings.getBattingTeam());
                int wicketsInHand = 10 - innings.getWickets();
                match.setResultMargin(innings.getBattingTeam().getName() + " won by " + wicketsInHand + (wicketsInHand == 1 ? " wicket" : " wickets"));
                matchRepository.save(match);
                return;
            }

            // 2nd innings overs finished or all out
            if (oversFinished || allOut) {
                innings.setIsCompleted(true);
                inningsRepository.save(innings);
                finalizeMatchResult(match, innings);
            }
        } else {
            // 1st innings overs finished or all out
            if (oversFinished || allOut) {
                innings.setIsCompleted(true);
                inningsRepository.save(innings);

                match.setStatus(MatchStatus.INNINGS_BREAK);
                int target = innings.getTotalRuns() + 1;

                // Set up 2nd innings
                Optional<Innings> innings2Opt = inningsRepository.findByMatchIdAndInningsNumber(match.getId(), 2);
                Innings innings2;
                if (innings2Opt.isPresent()) {
                    innings2 = innings2Opt.get();
                    innings2.setTargetRuns(target);
                } else {
                    innings2 = new Innings(match, 2, innings.getBowlingTeam(), innings.getBattingTeam());
                    innings2.setTargetRuns(target);
                }
                inningsRepository.save(innings2);
                match.setCurrentInningsNumber(2);
                matchRepository.save(match);
            }
        }
    }

    private void finalizeMatchResult(CricketMatch match, Innings innings2) {
        match.setStatus(MatchStatus.COMPLETED);
        int target = innings2.getTargetRuns() != null ? innings2.getTargetRuns() : 0;
        int chasingRuns = innings2.getTotalRuns();

        if (chasingRuns >= target) {
            match.setWinnerTeam(innings2.getBattingTeam());
            int w = 10 - innings2.getWickets();
            match.setResultMargin(innings2.getBattingTeam().getName() + " won by " + w + (w == 1 ? " wicket" : " wickets"));
        } else if (chasingRuns == target - 1) {
            match.setWinnerTeam(null);
            match.setResultMargin("Match Tied");
        } else {
            match.setWinnerTeam(innings2.getBowlingTeam());
            int runMargin = (target - 1) - chasingRuns;
            match.setResultMargin(innings2.getBowlingTeam().getName() + " won by " + runMargin + (runMargin == 1 ? " run" : " runs"));
        }
        matchRepository.save(match);
    }

    public BattingScore getOrCreateBattingScore(Innings innings, Player player) {
        return battingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), player.getId())
                .orElseGet(() -> {
                    List<BattingScore> existing = battingScoreRepository.findByInningsIdOrderByBattingOrderAsc(innings.getId());
                    BattingScore bs = new BattingScore(innings, player, existing.size() + 1);
                    return battingScoreRepository.save(bs);
                });
    }

    public BowlingScore getOrCreateBowlingScore(Innings innings, Player player) {
        return bowlingScoreRepository.findByInningsIdAndPlayerId(innings.getId(), player.getId())
                .orElseGet(() -> bowlingScoreRepository.save(new BowlingScore(innings, player)));
    }

    private String formatDismissalText(WicketType type, Player bowler, Player fielder) {
        switch (type) {
            case BOWLED:
                return "b " + bowler.getName();
            case CAUGHT:
                return (fielder != null ? "c " + fielder.getName() : "c &") + " b " + bowler.getName();
            case LBW:
                return "lbw b " + bowler.getName();
            case STUMPED:
                return "st " + (fielder != null ? fielder.getName() : "Keeper") + " b " + bowler.getName();
            case RUN_OUT:
                return "run out (" + (fielder != null ? fielder.getName() : "Direct") + ")";
            case HIT_WICKET:
                return "hit wicket b " + bowler.getName();
            default:
                return "out";
        }
    }

    private String buildAutomaticCommentary(Player bowler, Player striker, int runsBatter, ExtraType extraType, int extraRuns, boolean isWicket, WicketType wicketType) {
        String base = bowler.getName() + " to " + striker.getName() + ", ";
        if (isWicket) {
            return base + "OUT! (" + wicketType + ") Huge breakthrough for " + bowler.getTeam().getShortName() + "!";
        }
        if (extraType == ExtraType.WIDE) {
            return base + "Wide ball! Straying down the leg side.";
        }
        if (extraType == ExtraType.NO_BALL) {
            return base + "No ball called! Overstepping the crease. Free hit upcoming.";
        }
        if (extraType == ExtraType.BYE) {
            return base + extraRuns + " bye" + (extraRuns > 1 ? "s" : "") + " taken.";
        }
        if (extraType == ExtraType.LEG_BYE) {
            return base + extraRuns + " leg bye" + (extraRuns > 1 ? "s" : "") + " taken.";
        }
        if (runsBatter == 0) {
            return base + "no run, good solid defensive shot.";
        }
        if (runsBatter == 1) {
            return base + "1 run pushed into the gap for a quick single.";
        }
        if (runsBatter == 2) {
            return base + "2 runs, driven through deep cover, brilliant running!";
        }
        if (runsBatter == 3) {
            return base + "3 runs, timed neatly towards wide long-on.";
        }
        if (runsBatter == 4) {
            return base + "FOUR! Cracking shot through point, races to the fence!";
        }
        if (runsBatter == 6) {
            return base + "SIX! Massive strike! Clears deep mid-wicket into the stands!";
        }
        return base + runsBatter + " runs scored.";
    }

    @Transactional(readOnly = true)
    public LiveMatchSnapshotDto buildLiveMatchSnapshot(Long matchId) {
        CricketMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));

        LiveMatchSnapshotDto dto = new LiveMatchSnapshotDto();
        dto.setMatchId(match.getId());
        dto.setTitle(match.getTitle());
        dto.setVenue(match.getVenue());
        dto.setFormat(match.getFormat());
        dto.setTotalOvers(match.getTotalOvers());
        dto.setStatus(match.getStatus());
        dto.setResultMargin(match.getResultMargin());
        dto.setMatchSummary(match.getMatchSummary());

        if (match.getTossWinner() != null) {
            dto.setTossInfo(match.getTossWinner().getName() + " won the toss and elected to " +
                    (match.getTossDecision() != null ? match.getTossDecision().name().toLowerCase() : "bat") + " first");
        }

        // Find active innings
        List<Innings> inningsList = inningsRepository.findByMatchIdOrderByInningsNumberAsc(matchId);
        if (inningsList.isEmpty()) {
            return dto;
        }

        Innings currentInnings = inningsList.get(inningsList.size() - 1);
        for (Innings inn : inningsList) {
            if (Boolean.FALSE.equals(inn.getIsCompleted())) {
                currentInnings = inn;
                break;
            }
        }

        dto.setInningsId(currentInnings.getId());
        dto.setInningsNumber(currentInnings.getInningsNumber());
        dto.setBattingTeamId(currentInnings.getBattingTeam().getId());
        dto.setBattingTeamName(currentInnings.getBattingTeam().getName());
        dto.setBattingTeamShortName(currentInnings.getBattingTeam().getShortName());
        dto.setBattingTeamColor(currentInnings.getBattingTeam().getPrimaryColor());

        dto.setBowlingTeamId(currentInnings.getBowlingTeam().getId());
        dto.setBowlingTeamName(currentInnings.getBowlingTeam().getName());
        dto.setBowlingTeamShortName(currentInnings.getBowlingTeam().getShortName());
        dto.setBowlingTeamColor(currentInnings.getBowlingTeam().getPrimaryColor());

        dto.setTotalRuns(currentInnings.getTotalRuns());
        dto.setWickets(currentInnings.getWickets());
        dto.setOversFormatted(currentInnings.getOversFormatted());
        dto.setLegalBalls(currentInnings.getLegalBalls());
        dto.setCurrentRunRate(currentInnings.getCurrentRunRate());
        dto.setRequiredRunRate(currentInnings.getRequiredRunRate());
        dto.setTargetRuns(currentInnings.getTargetRuns());

        if (currentInnings.getTargetRuns() != null) {
            dto.setRunsNeeded(Math.max(0, currentInnings.getTargetRuns() - currentInnings.getTotalRuns()));
            int totalMatchBalls = match.getTotalOvers() * 6;
            dto.setRemainingBalls(Math.max(0, totalMatchBalls - currentInnings.getLegalBalls()));
        }

        dto.setInningsCompleted(Boolean.TRUE.equals(currentInnings.getIsCompleted()));
        dto.setMatchCompleted(match.getStatus() == MatchStatus.COMPLETED);

        // Striker & Non-striker DTO
        if (currentInnings.getCurrentStriker() != null) {
            Optional<BattingScore> bs = battingScoreRepository.findByInningsIdAndPlayerId(currentInnings.getId(), currentInnings.getCurrentStriker().getId());
            bs.ifPresent(score -> dto.setStriker(new LiveMatchSnapshotDto.BatterLiveDto(
                    score.getPlayer().getId(),
                    score.getPlayer().getName(),
                    score.getRuns(),
                    score.getBallsFaced(),
                    score.getFours(),
                    score.getSixes(),
                    score.getStrikeRate()
            )));
        }

        if (currentInnings.getCurrentNonStriker() != null) {
            Optional<BattingScore> bs = battingScoreRepository.findByInningsIdAndPlayerId(currentInnings.getId(), currentInnings.getCurrentNonStriker().getId());
            bs.ifPresent(score -> dto.setNonStriker(new LiveMatchSnapshotDto.BatterLiveDto(
                    score.getPlayer().getId(),
                    score.getPlayer().getName(),
                    score.getRuns(),
                    score.getBallsFaced(),
                    score.getFours(),
                    score.getSixes(),
                    score.getStrikeRate()
            )));
        }

        // Bowler DTO
        if (currentInnings.getCurrentBowler() != null) {
            Optional<BowlingScore> bs = bowlingScoreRepository.findByInningsIdAndPlayerId(currentInnings.getId(), currentInnings.getCurrentBowler().getId());
            bs.ifPresent(score -> dto.setCurrentBowler(new LiveMatchSnapshotDto.BowlerLiveDto(
                    score.getPlayer().getId(),
                    score.getPlayer().getName(),
                    score.getOversFormatted(),
                    score.getMaidens(),
                    score.getRunsConceded(),
                    score.getWickets(),
                    score.getEconomy()
            )));
        }

        // Recent deliveries (last 12 balls)
        List<Delivery> recentDeliveries = deliveryRepository.findTop12ByInningsIdOrderBySequenceOrderDesc(currentInnings.getId());
        dto.setRecentBalls(recentDeliveries.stream().map(d -> {
            String display;
            if (Boolean.TRUE.equals(d.getIsWicket())) {
                display = "W";
            } else if (d.getExtraType() == ExtraType.WIDE) {
                display = (d.getRunsExtra() + d.getRunsBatter()) + "wd";
            } else if (d.getExtraType() == ExtraType.NO_BALL) {
                display = (d.getRunsExtra() + d.getRunsBatter()) + "nb";
            } else if (d.getExtraType() == ExtraType.BYE) {
                display = d.getRunsExtra() + "b";
            } else if (d.getExtraType() == ExtraType.LEG_BYE) {
                display = d.getRunsExtra() + "lb";
            } else {
                display = String.valueOf(d.getRunsBatter());
            }
            boolean isBoundary = d.getRunsBatter() == 4 || d.getRunsBatter() == 6;
            boolean isWicket = Boolean.TRUE.equals(d.getIsWicket());
            return new LiveMatchSnapshotDto.RecentBallDto(d.getId(), display, d.getOversAtBallFormatted(), isBoundary, isWicket);
        }).toList());

        // Recent commentary
        dto.setRecentCommentary(recentDeliveries.stream().map(d -> {
            String badge = "";
            if (Boolean.TRUE.equals(d.getIsWicket())) badge = "W";
            else if (d.getRunsBatter() == 6) badge = "6";
            else if (d.getRunsBatter() == 4) badge = "4";
            else if (d.getExtraType() == ExtraType.WIDE) badge = "WD";
            else if (d.getExtraType() == ExtraType.NO_BALL) badge = "NB";
            return new LiveMatchSnapshotDto.CommentaryItemDto(d.getOversAtBallFormatted(), d.getCommentary(), badge);
        }).toList());

        return dto;
    }
}
