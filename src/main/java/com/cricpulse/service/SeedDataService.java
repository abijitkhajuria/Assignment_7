package com.cricpulse.service;

import com.cricpulse.entity.*;
import com.cricpulse.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeedDataService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataService.class);

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final BattingScoreRepository battingScoreRepository;
    private final BowlingScoreRepository bowlingScoreRepository;
    private final DeliveryRepository deliveryRepository;
    private final FallOfWicketRepository fallOfWicketRepository;
    private final AdminUserRepository adminUserRepository;

    public SeedDataService(
            TeamRepository teamRepository,
            PlayerRepository playerRepository,
            MatchRepository matchRepository,
            InningsRepository inningsRepository,
            BattingScoreRepository battingScoreRepository,
            BowlingScoreRepository bowlingScoreRepository,
            DeliveryRepository deliveryRepository,
            FallOfWicketRepository fallOfWicketRepository,
            AdminUserRepository adminUserRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.inningsRepository = inningsRepository;
        this.battingScoreRepository = battingScoreRepository;
        this.bowlingScoreRepository = bowlingScoreRepository;
        this.deliveryRepository = deliveryRepository;
        this.fallOfWicketRepository = fallOfWicketRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        boolean hasAccurateSeries = matchRepository.findAll().stream()
                .anyMatch(m -> m.getTitle() != null && m.getTitle().contains("India tour of South Africa"));
        if (hasAccurateSeries) {
            log.info("Database already seeded with accurate current 2024 international matches.");
            return;
        }

        log.info("Refreshing CricPulse database with accurate modern international fixtures (Live Today, Tomorrow, Results)...");
        reseedAccurateFixtures();
    }

    @Transactional
    public void reseedAccurateFixtures() {
        // Clean out legacy/dummy match and delivery records
        deliveryRepository.deleteAll();
        deliveryRepository.flush();
        fallOfWicketRepository.deleteAll();
        fallOfWicketRepository.flush();
        battingScoreRepository.deleteAll();
        battingScoreRepository.flush();
        bowlingScoreRepository.deleteAll();
        bowlingScoreRepository.flush();
        inningsRepository.deleteAll();
        inningsRepository.flush();
        matchRepository.deleteAll();
        matchRepository.flush();
        playerRepository.deleteAll();
        playerRepository.flush();

        // 1. Admin User
        if (adminUserRepository.count() == 0) {
            adminUserRepository.save(new AdminUser("admin", "admin123", "System Administrator", "ROLE_ADMIN"));
        }

        // 2. 6 International Cricket Teams (Get or update existing, or create)
        Team ind = getOrCreateTeam("India", "IND", "/images/flags/ind.png", "#0077B6");
        Team sa = getOrCreateTeam("South Africa", "SA", "/images/flags/sa.png", "#007A3D");
        Team nz = getOrCreateTeam("New Zealand", "NZ", "/images/flags/nz.png", "#1E293B");
        Team aus = getOrCreateTeam("Australia", "AUS", "/images/flags/aus.png", "#F59E0B");
        Team pak = getOrCreateTeam("Pakistan", "PAK", "/images/flags/pak.png", "#10B981");
        Team eng = getOrCreateTeam("England", "ENG", "/images/flags/eng.png", "#C8102E");

        // 3. Players for India (IND)
        Player surya = createPlayer("Suryakumar Yadav", ind, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 63, 72, 2450, 117, 42.8, 169.2, 0, 0.0, "-", 0.0);
        Player sanju = createPlayer("Sanju Samson", ind, PlayerRole.WICKET_KEEPER, "Right-hand bat", "-", 9, 34, 780, 111, 28.9, 152.4, 0, 0.0, "-", 0.0);
        Player abhishek = createPlayer("Abhishek Sharma", ind, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Slow left-arm orthodox", 4, 9, 210, 100, 23.3, 172.1, 3, 24.0, "1/14", 7.8);
        Player tilak = createPlayer("Tilak Varma", ind, PlayerRole.BATSMAN, "Left-hand bat", "Right-arm offbreak", 72, 17, 420, 120, 38.2, 144.8, 2, 19.5, "1/5", 7.1);
        Player hardik = createPlayer("Hardik Pandya", ind, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm fast-medium", 33, 106, 1620, 71, 27.5, 141.6, 88, 26.1, "4/16", 8.1);
        Player rinku = createPlayer("Rinku Singh", ind, PlayerRole.BATSMAN, "Left-hand bat", "Right-arm offbreak", 35, 27, 495, 69, 55.0, 171.3, 2, 9.0, "2/0", 6.0);
        Player axar = createPlayer("Axar Patel", ind, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Slow left-arm orthodox", 20, 62, 450, 65, 20.4, 140.6, 62, 23.2, "3/9", 7.2);
        Player varun = createPlayer("Varun Chakaravarthy", ind, PlayerRole.BOWLER, "Right-hand bat", "Right-arm legbreak", 29, 10, 12, 5, 6.0, 60.0, 15, 14.8, "5/17", 6.8);
        Player arshdeep = createPlayer("Arshdeep Singh", ind, PlayerRole.BOWLER, "Left-hand bat", "Left-arm medium-fast", 2, 58, 28, 9, 7.0, 75.0, 92, 18.2, "4/9", 8.3);
        Player bishnoi = createPlayer("Ravi Bishnoi", ind, PlayerRole.BOWLER, "Right-hand bat", "Right-arm legbreak", 56, 33, 25, 8, 8.3, 71.4, 51, 18.9, "4/16", 7.2);
        Player rohit = createPlayer("Rohit Sharma", ind, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 45, 159, 4231, 121, 32.1, 140.9, 1, 0.0, "1/22", 8.2);
        Player virat = createPlayer("Virat Kohli", ind, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm medium", 18, 125, 4188, 122, 48.7, 137.9, 4, 20.0, "1/13", 8.1);
        Player rishabh = createPlayer("Rishabh Pant", ind, PlayerRole.WICKET_KEEPER, "Left-hand bat", "-", 17, 76, 1205, 65, 23.6, 126.8, 0, 0.0, "-", 0.0);
        Player bumrah = createPlayer("Jasprit Bumrah", ind, PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", 93, 70, 75, 14, 8.3, 68.2, 89, 17.7, "3/7", 6.2);
        Player jadeja = createPlayer("Ravindra Jadeja", ind, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Slow left-arm orthodox", 8, 74, 515, 46, 21.4, 127.1, 54, 28.5, "3/15", 7.1);
        Player sundar = createPlayer("Washington Sundar", ind, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Right-arm offbreak", 5, 52, 161, 50, 16.1, 121.1, 47, 23.4, "7/59", 6.9);
        Player jaiswal = createPlayer("Yashasvi Jaiswal", ind, PlayerRole.BATSMAN, "Left-hand bat", "Right-arm legbreak", 64, 23, 723, 100, 36.1, 164.3, 1, 12.0, "1/5", 6.5);
        Player gill = createPlayer("Shubman Gill", ind, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 77, 21, 578, 126, 30.4, 139.3, 0, 0.0, "-", 0.0);
        Player sarfaraz = createPlayer("Sarfaraz Khan", ind, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm legbreak", 97, 6, 371, 150, 46.3, 125.0, 0, 0.0, "-", 0.0);

        // Players for South Africa (SA)
        Player markram = createPlayer("Aiden Markram", sa, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 4, 50, 1240, 70, 31.0, 146.5, 12, 25.4, "2/8", 7.5);
        Player rickelton = createPlayer("Ryan Rickelton", sa, PlayerRole.WICKET_KEEPER, "Left-hand bat", "-", 44, 12, 230, 44, 20.9, 133.7, 0, 0.0, "-", 0.0);
        Player stubbs = createPlayer("Tristan Stubbs", sa, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 30, 26, 480, 72, 26.6, 148.1, 1, 15.0, "1/2", 8.0);
        Player klaasen = createPlayer("Heinrich Klaasen", sa, PlayerRole.WICKET_KEEPER, "Right-hand bat", "Right-arm offbreak", 45, 52, 912, 81, 24.0, 143.6, 0, 0.0, "-", 0.0);
        Player miller = createPlayer("David Miller", sa, PlayerRole.BATSMAN, "Left-hand bat", "Right-arm offbreak", 10, 126, 2437, 106, 33.8, 140.5, 0, 0.0, "-", 0.0);
        Player jansen = createPlayer("Marco Jansen", sa, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Left-arm fast", 70, 18, 155, 30, 17.2, 128.1, 18, 30.2, "3/52", 8.8);
        Player coetzee = createPlayer("Gerald Coetzee", sa, PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", 62, 8, 75, 23, 15.0, 150.0, 13, 21.0, "3/32", 9.1);
        Player maharaj = createPlayer("Keshav Maharaj", sa, PlayerRole.BOWLER, "Right-hand bat", "Slow left-arm orthodox", 16, 34, 125, 24, 12.5, 110.0, 39, 24.3, "3/27", 7.4);

        // Players for New Zealand (NZ)
        Player latham = createPlayer("Tom Latham", nz, PlayerRole.BATSMAN, "Left-hand bat", "-", 48, 84, 5630, 264, 40.2, 78.4, 0, 0.0, "-", 0.0);
        Player conway = createPlayer("Devon Conway", nz, PlayerRole.BATSMAN, "Left-hand bat", "Right-arm medium", 88, 48, 1450, 99, 38.2, 128.5, 0, 0.0, "-", 0.0);
        Player ravindra = createPlayer("Rachin Ravindra", nz, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Slow left-arm orthodox", 8, 22, 620, 134, 41.3, 138.0, 14, 26.5, "3/29", 7.3);
        Player daryl = createPlayer("Daryl Mitchell", nz, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm medium", 75, 68, 1350, 72, 30.0, 136.2, 8, 31.0, "2/27", 8.9);
        Player phillips = createPlayer("Glenn Phillips", nz, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm offbreak", 23, 78, 1850, 108, 32.4, 143.8, 19, 22.1, "3/21", 7.6);
        Player santner = createPlayer("Mitchell Santner", nz, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Slow left-arm orthodox", 74, 105, 680, 77, 16.5, 123.4, 115, 22.2, "7/53", 7.0);
        Player southee = createPlayer("Tim Southee", nz, PlayerRole.BOWLER, "Right-hand bat", "Right-arm medium-fast", 38, 126, 380, 39, 10.2, 135.0, 164, 22.4, "5/18", 8.1);
        Player ajaz = createPlayer("Ajaz Patel", nz, PlayerRole.BOWLER, "Left-hand bat", "Slow left-arm orthodox", 24, 18, 140, 35, 11.6, 68.0, 70, 29.8, "10/119", 3.2);

        // Players for Australia (AUS)
        Player cummins = createPlayer("Pat Cummins", aus, PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", 30, 57, 178, 28, 11.8, 128.9, 66, 23.3, "3/15", 7.3);
        Player head = createPlayer("Travis Head", aus, PlayerRole.BATSMAN, "Left-hand bat", "Right-arm offbreak", 62, 38, 955, 91, 32.9, 158.4, 1, 35.0, "1/12", 8.9);
        Player marsh = createPlayer("Mitchell Marsh", aus, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm medium", 8, 54, 1432, 92, 34.1, 135.2, 17, 28.2, "3/24", 8.2);
        Player maxwell = createPlayer("Glenn Maxwell", aus, PlayerRole.ALL_ROUNDER, "Right-hand bat", "Right-arm offbreak", 32, 106, 2600, 145, 29.5, 155.5, 43, 27.4, "3/10", 7.5);
        Player starc = createPlayer("Mitchell Starc", aus, PlayerRole.BOWLER, "Left-hand bat", "Left-arm fast", 56, 65, 94, 14, 9.4, 98.9, 79, 23.8, "4/20", 7.7);
        Player zampa = createPlayer("Adam Zampa", aus, PlayerRole.BOWLER, "Right-hand bat", "Right-arm legbreak", 88, 92, 55, 13, 6.1, 75.0, 115, 21.8, "5/19", 7.1);

        // Players for Pakistan (PAK)
        Player rizwan = createPlayer("Mohammad Rizwan", pak, PlayerRole.WICKET_KEEPER, "Right-hand bat", "-", 16, 102, 3313, 98, 48.7, 126.1, 0, 0.0, "-", 0.0);
        Player babar = createPlayer("Babar Azam", pak, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 56, 123, 4145, 122, 41.0, 129.1, 0, 0.0, "-", 0.0);
        Player shaheen = createPlayer("Shaheen Shah Afridi", pak, PlayerRole.BOWLER, "Left-hand bat", "Left-arm fast", 10, 70, 195, 23, 10.2, 130.0, 96, 20.8, "4/22", 7.7);
        Player rauf = createPlayer("Haris Rauf", pak, PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", 150, 72, 85, 15, 7.7, 95.0, 102, 20.5, "4/18", 8.2);
        Player naseem = createPlayer("Naseem Shah", pak, PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", 71, 28, 40, 14, 8.0, 110.0, 35, 23.1, "3/21", 7.6);
        Player sajid = createPlayer("Sajid Khan", pak, PlayerRole.BOWLER, "Right-hand bat", "Right-arm offbreak", 39, 10, 115, 48, 12.7, 72.0, 44, 28.1, "6/128", 3.4);
        Player shakeel = createPlayer("Saud Shakeel", pak, PlayerRole.BATSMAN, "Left-hand bat", "Slow left-arm orthodox", 59, 15, 1180, 208, 56.1, 75.2, 1, 45.0, "1/14", 3.8);

        // Players for England (ENG)
        Player buttler = createPlayer("Jos Buttler", eng, PlayerRole.WICKET_KEEPER, "Right-hand bat", "-", 63, 124, 3264, 101, 35.8, 145.3, 0, 0.0, "-", 0.0);
        Player salt = createPlayer("Phil Salt", eng, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 28, 31, 885, 119, 35.4, 166.0, 0, 0.0, "-", 0.0);
        Player root = createPlayer("Joe Root", eng, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm offbreak", 66, 148, 12754, 262, 51.2, 68.0, 69, 44.5, "5/8", 3.1);
        Player brook = createPlayer("Harry Brook", eng, PlayerRole.BATSMAN, "Right-hand bat", "Right-arm medium", 88, 39, 705, 81, 30.6, 146.8, 0, 0.0, "-", 0.0);
        Player stokes = createPlayer("Ben Stokes", eng, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Right-arm fast-medium", 55, 105, 6508, 258, 35.7, 85.0, 203, 32.0, "6/22", 3.3);
        Player curran = createPlayer("Sam Curran", eng, PlayerRole.ALL_ROUNDER, "Left-hand bat", "Left-arm medium-fast", 58, 51, 268, 41, 13.4, 125.8, 52, 24.3, "5/10", 8.1);
        Player archer = createPlayer("Jofra Archer", eng, PlayerRole.BOWLER, "Right-hand bat", "Right-arm fast", 22, 25, 68, 17, 11.3, 115.2, 31, 22.1, "4/33", 7.6);
        Player rashid = createPlayer("Adil Rashid", eng, PlayerRole.BOWLER, "Right-hand bat", "Right-arm legbreak", 95, 114, 118, 22, 7.8, 85.5, 120, 25.1, "4/2", 7.4);

        // =========================================================================
        // 4. MATCH 1: LIVE TODAY (India vs South Africa — 1st T20I, Durban)
        // =========================================================================
        CricketMatch liveMatch = new CricketMatch();
        liveMatch.setTitle("1st T20I — India tour of South Africa 2024");
        liveMatch.setSeriesName("India tour of South Africa 2024 (4-Match T20I Series)");
        liveMatch.setDayNote("South Africa need 58 runs in 18 balls • Target 203");
        liveMatch.setVenue("Kingsmead Cricket Ground, Durban");
        liveMatch.setMatchDate(LocalDateTime.now().minusHours(2));
        liveMatch.setFormat(MatchFormat.T20);
        liveMatch.setTotalOvers(20);
        liveMatch.setTeam1(ind);
        liveMatch.setTeam2(sa);
        liveMatch.setTossWinner(sa);
        liveMatch.setTossDecision(TossDecision.BOWL);
        liveMatch.setStatus(MatchStatus.LIVE);
        liveMatch.setCurrentInningsNumber(2);
        liveMatch = matchRepository.save(liveMatch);

        // Innings 1 (India: 202/8 in 20.0 ov - Sanju Samson Century)
        Innings indInnings = new Innings(liveMatch, 1, ind, sa);
        indInnings.setTotalRuns(202);
        indInnings.setWickets(8);
        indInnings.setLegalBalls(120);
        indInnings.setIsCompleted(true);
        indInnings.setExtrasWide(5);
        indInnings.setExtrasNoBall(1);
        indInnings.setExtrasBye(0);
        indInnings.setExtrasLegBye(4);
        indInnings = inningsRepository.save(indInnings);

        saveBatting(indInnings, sanju, 1, 107, 50, 7, 10, true, WicketType.CAUGHT, coetzee, stubbs, "c Stubbs b Coetzee");
        saveBatting(indInnings, abhishek, 2, 7, 8, 1, 0, true, WicketType.CAUGHT, coetzee, markram, "c Markram b Coetzee");
        saveBatting(indInnings, surya, 3, 21, 17, 2, 1, true, WicketType.CAUGHT, maharaj, rickelton, "c Rickelton b Maharaj");
        saveBatting(indInnings, tilak, 4, 33, 18, 3, 2, true, WicketType.CAUGHT, jansen, miller, "c Miller b Jansen");
        saveBatting(indInnings, hardik, 5, 2, 6, 0, 0, true, WicketType.CAUGHT, coetzee, rickelton, "c Rickelton b Coetzee");
        saveBatting(indInnings, rinku, 6, 11, 10, 1, 0, true, WicketType.BOWLED, jansen, null, "b Jansen");
        saveBatting(indInnings, axar, 7, 7, 4, 1, 0, true, WicketType.RUN_OUT, null, markram, "run out (Markram)");
        saveBatting(indInnings, arshdeep, 8, 5, 4, 1, 0, false, WicketType.NONE, null, null, "not out");
        saveBatting(indInnings, varun, 9, 0, 3, 0, 0, false, WicketType.NONE, null, null, "not out");

        saveBowling(indInnings, jansen, 24, 0, 37, 2);
        saveBowling(indInnings, coetzee, 24, 0, 37, 3);
        saveBowling(indInnings, maharaj, 24, 0, 34, 1);
        saveBowling(indInnings, markram, 12, 0, 24, 0);

        saveFow(indInnings, 1, 24, "2.4", abhishek);
        saveFow(indInnings, 2, 90, "8.4", surya);
        saveFow(indInnings, 3, 167, "14.4", tilak);
        saveFow(indInnings, 4, 175, "15.3", sanju);
        saveFow(indInnings, 5, 181, "16.4", hardik);
        saveFow(indInnings, 6, 194, "18.3", rinku);
        saveFow(indInnings, 7, 202, "19.6", axar);

        // Innings 2 (South Africa: Chasing 203: 145/6 in 17.0 ov)
        Innings saInnings = new Innings(liveMatch, 2, sa, ind);
        saInnings.setTotalRuns(145);
        saInnings.setWickets(6);
        saInnings.setLegalBalls(102); // 17.0 ov
        saInnings.setTargetRuns(203);
        saInnings.setIsCompleted(false);
        saInnings.setExtrasWide(4);
        saInnings.setExtrasNoBall(1);
        saInnings.setExtrasBye(0);
        saInnings.setExtrasLegBye(2);
        saInnings.setCurrentStriker(coetzee);
        saInnings.setCurrentNonStriker(jansen);
        saInnings.setCurrentBowler(arshdeep);
        saInnings = inningsRepository.save(saInnings);

        saveBatting(saInnings, rickelton, 1, 21, 11, 3, 1, true, WicketType.CAUGHT, varun, tilak, "c Varma b Chakaravarthy");
        saveBatting(saInnings, markram, 2, 8, 8, 1, 0, true, WicketType.CAUGHT, arshdeep, sanju, "c Samson b Arshdeep");
        saveBatting(saInnings, stubbs, 3, 11, 11, 1, 0, true, WicketType.LBW, bishnoi, null, "lbw b Bishnoi");
        saveBatting(saInnings, klaasen, 4, 25, 22, 2, 1, true, WicketType.CAUGHT, varun, axar, "c Axar b Chakaravarthy");
        saveBatting(saInnings, miller, 5, 18, 14, 1, 1, true, WicketType.CAUGHT, varun, surya, "c Yadav b Chakaravarthy");
        saveBatting(saInnings, coetzee, 6, 23, 12, 1, 2, false, WicketType.NONE, null, null, "not out");
        saveBatting(saInnings, jansen, 7, 12, 8, 1, 0, false, WicketType.NONE, null, null, "not out");

        saveBowling(saInnings, arshdeep, 18, 0, 25, 1);
        saveBowling(saInnings, varun, 24, 0, 25, 3);
        saveBowling(saInnings, bishnoi, 24, 0, 28, 2);
        saveBowling(saInnings, axar, 18, 0, 26, 0);
        saveBowling(saInnings, hardik, 18, 0, 39, 0);

        saveFow(saInnings, 1, 22, "1.5", markram);
        saveFow(saInnings, 2, 44, "3.5", rickelton);
        saveFow(saInnings, 3, 62, "6.3", stubbs);
        saveFow(saInnings, 4, 87, "9.5", klaasen);
        saveFow(saInnings, 5, 108, "12.2", miller);

        // Commentary / Over 17 deliveries
        saveDelivery(saInnings, 16, 1, 97, jansen, coetzee, arshdeep, 1, 0, ExtraType.NONE, true, false, null, null, null, "Arshdeep to Jansen, 1 run, pinpoint yorker on off stump, dug out to mid-off.", 137, 6, "16.1", 1);
        saveDelivery(saInnings, 16, 2, 98, coetzee, jansen, arshdeep, 6, 0, ExtraType.NONE, true, false, null, null, null, "Arshdeep to Coetzee, SIX! Smacked over wide long-on with immense power into the crowd!", 143, 6, "16.2", 2);
        saveDelivery(saInnings, 16, 3, 98, coetzee, jansen, arshdeep, 0, 1, ExtraType.WIDE, false, false, null, null, null, "Arshdeep to Coetzee, Wide ball! Slower ball slipped well outside off tramline.", 144, 6, "16.2", 3);
        saveDelivery(saInnings, 16, 4, 99, coetzee, jansen, arshdeep, 0, 0, ExtraType.NONE, true, false, null, null, null, "Arshdeep to Coetzee, no run, searing bouncer! Ducked under safely.", 144, 6, "16.3", 4);
        saveDelivery(saInnings, 16, 5, 100, coetzee, jansen, arshdeep, 0, 0, ExtraType.NONE, true, false, null, null, null, "Arshdeep to Coetzee, no run, swing and a miss outside off. Excellent comeback.", 144, 6, "16.4", 5);
        saveDelivery(saInnings, 16, 6, 101, coetzee, jansen, arshdeep, 1, 0, ExtraType.NONE, true, false, null, null, null, "Arshdeep to Coetzee, 1 run, driven firmly down to long-on for a single.", 145, 6, "16.5", 6);
        saveDelivery(saInnings, 16, 7, 102, jansen, coetzee, arshdeep, 0, 0, ExtraType.NONE, true, false, null, null, null, "Arshdeep to Jansen, no run, laser-guided yorker on leg stump. Defended.", 145, 6, "17.0", 7);

        // =========================================================================
        // 5. MATCH 2: LIVE TODAY (India vs New Zealand — 2nd Test, Pune)
        // =========================================================================
        CricketMatch testMatch = new CricketMatch();
        testMatch.setTitle("2nd Test — New Zealand tour of India 2024");
        testMatch.setSeriesName("New Zealand tour of India 2024 (3-Test Series)");
        testMatch.setDayNote("Day 2: Stumps — NZ lead by 301 runs (Sundar 7/59, Santner 7/53)");
        testMatch.setVenue("MCA Stadium, Pune");
        testMatch.setMatchDate(LocalDateTime.now().minusHours(5));
        testMatch.setFormat(MatchFormat.TEST);
        testMatch.setTotalOvers(90);
        testMatch.setTeam1(nz);
        testMatch.setTeam2(ind);
        testMatch.setTossWinner(nz);
        testMatch.setTossDecision(TossDecision.BAT);
        testMatch.setStatus(MatchStatus.LIVE);
        testMatch.setCurrentInningsNumber(2);
        testMatch = matchRepository.save(testMatch);

        // NZ 1st Innings: 259 all out (79.1 ov)
        Innings nzInn1 = new Innings(testMatch, 1, nz, ind);
        nzInn1.setTotalRuns(259);
        nzInn1.setWickets(10);
        nzInn1.setLegalBalls(475);
        nzInn1.setIsCompleted(true);
        nzInn1.setExtrasBye(2);
        nzInn1.setExtrasLegBye(4);
        nzInn1 = inningsRepository.save(nzInn1);

        saveBatting(nzInn1, latham, 1, 15, 45, 2, 0, true, WicketType.LBW, sundar, null, "lbw b Sundar");
        saveBatting(nzInn1, conway, 2, 76, 141, 11, 0, true, WicketType.CAUGHT, sundar, rishabh, "c Pant b Sundar");
        saveBatting(nzInn1, ravindra, 3, 65, 105, 5, 1, true, WicketType.BOWLED, sundar, null, "b Sundar");
        saveBatting(nzInn1, daryl, 4, 18, 54, 2, 0, true, WicketType.CAUGHT, sundar, jaiswal, "c Jaiswal b Sundar");
        saveBatting(nzInn1, santner, 5, 33, 51, 3, 2, true, WicketType.BOWLED, sundar, null, "b Sundar");

        saveBowling(nzInn1, sundar, 143, 4, 59, 7);
        saveBowling(nzInn1, bumrah, 48, 1, 32, 0);

        saveFow(nzInn1, 1, 32, "7.5", latham);
        saveFow(nzInn1, 2, 138, "43.2", conway);
        saveFow(nzInn1, 3, 197, "59.1", ravindra);

        // IND 1st Innings: 156 all out (45.3 ov)
        Innings indInn1 = new Innings(testMatch, 2, ind, nz);
        indInn1.setTotalRuns(156);
        indInn1.setWickets(10);
        indInn1.setLegalBalls(273);
        indInn1.setIsCompleted(true);
        indInn1.setExtrasBye(1);
        indInn1.setExtrasLegBye(3);
        indInn1 = inningsRepository.save(indInn1);

        saveBatting(indInn1, rohit, 1, 0, 9, 0, 0, true, WicketType.BOWLED, southee, null, "b Southee");
        saveBatting(indInn1, jaiswal, 2, 30, 60, 4, 0, true, WicketType.CAUGHT, phillips, daryl, "c Mitchell b Phillips");
        saveBatting(indInn1, gill, 3, 30, 72, 2, 1, true, WicketType.LBW, santner, null, "lbw b Santner");
        saveBatting(indInn1, virat, 4, 1, 9, 0, 0, true, WicketType.BOWLED, santner, null, "b Santner");
        saveBatting(indInn1, rishabh, 5, 18, 19, 2, 0, true, WicketType.BOWLED, phillips, null, "b Phillips");
        saveBatting(indInn1, jadeja, 6, 38, 46, 3, 2, true, WicketType.LBW, santner, null, "lbw b Santner");

        saveBowling(indInn1, santner, 117, 1, 53, 7);
        saveBowling(indInn1, phillips, 36, 0, 26, 2);
        saveBowling(indInn1, southee, 54, 1, 18, 1);

        saveFow(indInn1, 1, 1, "2.6", rohit);
        saveFow(indInn1, 2, 50, "21.3", gill);
        saveFow(indInn1, 3, 56, "23.4", virat);
        saveFow(indInn1, 4, 83, "30.2", rishabh);
        saveFow(indInn1, 5, 103, "37.5", jaiswal);

        // =========================================================================
        // 6. MATCH 3: TOMORROW (Australia vs Pakistan — 1st ODI, MCG)
        // =========================================================================
        CricketMatch odiMatch = new CricketMatch();
        odiMatch.setTitle("1st ODI — Pakistan tour of Australia 2024");
        odiMatch.setSeriesName("Pakistan tour of Australia 2024 (3-Match ODI Series)");
        odiMatch.setDayNote("Starts Tomorrow at 09:00 AM IST (02:30 PM Local)");
        odiMatch.setVenue("Melbourne Cricket Ground (MCG), Melbourne");
        odiMatch.setMatchDate(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
        odiMatch.setFormat(MatchFormat.ODI);
        odiMatch.setTotalOvers(50);
        odiMatch.setTeam1(aus);
        odiMatch.setTeam2(pak);
        odiMatch.setStatus(MatchStatus.SCHEDULED);
        matchRepository.save(odiMatch);

        // =========================================================================
        // 7. MATCH 4: TOMORROW (India vs South Africa — 2nd T20I, Gqeberha)
        // =========================================================================
        CricketMatch t20Match2 = new CricketMatch();
        t20Match2.setTitle("2nd T20I — India tour of South Africa 2024");
        t20Match2.setSeriesName("India tour of South Africa 2024 (4-Match T20I Series)");
        t20Match2.setDayNote("Starts Tomorrow at 07:30 PM IST (04:00 PM Local)");
        t20Match2.setVenue("St George's Park, Gqeberha");
        t20Match2.setMatchDate(LocalDateTime.now().plusDays(1).withHour(19).withMinute(30));
        t20Match2.setFormat(MatchFormat.T20);
        t20Match2.setTotalOvers(20);
        t20Match2.setTeam1(ind);
        t20Match2.setTeam2(sa);
        t20Match2.setStatus(MatchStatus.SCHEDULED);
        matchRepository.save(t20Match2);

        // =========================================================================
        // 8. MATCH 5: RECENT RESULT (India vs New Zealand — 1st Test, Bengaluru)
        // =========================================================================
        CricketMatch resMatch1 = new CricketMatch();
        resMatch1.setTitle("1st Test — New Zealand tour of India 2024");
        resMatch1.setSeriesName("New Zealand tour of India 2024 (3-Test Series)");
        resMatch1.setDayNote("New Zealand won by 8 wickets (First win in India in 36 years)");
        resMatch1.setVenue("M. Chinnaswamy Stadium, Bengaluru");
        resMatch1.setMatchDate(LocalDateTime.now().minusDays(2));
        resMatch1.setFormat(MatchFormat.TEST);
        resMatch1.setTotalOvers(90);
        resMatch1.setTeam1(ind);
        resMatch1.setTeam2(nz);
        resMatch1.setTossWinner(ind);
        resMatch1.setTossDecision(TossDecision.BAT);
        resMatch1.setStatus(MatchStatus.COMPLETED);
        resMatch1.setWinnerTeam(nz);
        resMatch1.setResultMargin("New Zealand won by 8 wickets");
        resMatch1.setPlayerOfTheMatch(ravindra);
        resMatch1.setMatchSummary("Rachin Ravindra's superb 134 in the first innings and an unbeaten 39 in the fourth-innings chase steered New Zealand to a historic 8-wicket triumph over India at Bengaluru — their first Test victory on Indian soil since 1988.");
        resMatch1 = matchRepository.save(resMatch1);

        Innings indM1Inn1 = new Innings(resMatch1, 1, ind, nz);
        indM1Inn1.setTotalRuns(462);
        indM1Inn1.setWickets(10);
        indM1Inn1.setLegalBalls(594);
        indM1Inn1.setIsCompleted(true);
        indM1Inn1 = inningsRepository.save(indM1Inn1);
        saveBatting(indM1Inn1, sarfaraz, 1, 150, 195, 18, 3, true, WicketType.CAUGHT, southee, ajaz, "c Patel b Southee");
        saveBatting(indM1Inn1, rishabh, 2, 99, 105, 9, 5, true, WicketType.BOWLED, southee, null, "b Southee");
        saveBowling(indM1Inn1, southee, 120, 2, 88, 3);

        Innings nzM1Inn2 = new Innings(resMatch1, 2, nz, ind);
        nzM1Inn2.setTotalRuns(110);
        nzM1Inn2.setWickets(2);
        nzM1Inn2.setLegalBalls(166);
        nzM1Inn2.setTargetRuns(107);
        nzM1Inn2.setIsCompleted(true);
        nzM1Inn2 = inningsRepository.save(nzM1Inn2);
        saveBatting(nzM1Inn2, ravindra, 1, 39, 46, 6, 0, false, WicketType.NONE, null, null, "not out");
        saveBowling(nzM1Inn2, bumrah, 48, 1, 29, 2);

        // =========================================================================
        // 9. MATCH 6: RECENT RESULT (Pakistan vs England — 3rd Test, Rawalpindi)
        // =========================================================================
        CricketMatch resMatch2 = new CricketMatch();
        resMatch2.setTitle("3rd Test — England tour of Pakistan 2024");
        resMatch2.setSeriesName("England tour of Pakistan 2024 (3-Test Series)");
        resMatch2.setDayNote("Pakistan won by 9 wickets (Series won 2-1)");
        resMatch2.setVenue("Rawalpindi Cricket Stadium, Rawalpindi");
        resMatch2.setMatchDate(LocalDateTime.now().minusDays(1));
        resMatch2.setFormat(MatchFormat.TEST);
        resMatch2.setTotalOvers(90);
        resMatch2.setTeam1(eng);
        resMatch2.setTeam2(pak);
        resMatch2.setTossWinner(eng);
        resMatch2.setTossDecision(TossDecision.BAT);
        resMatch2.setStatus(MatchStatus.COMPLETED);
        resMatch2.setWinnerTeam(pak);
        resMatch2.setResultMargin("Pakistan won by 9 wickets (Series won 2-1)");
        resMatch2.setPlayerOfTheMatch(shakeel);
        resMatch2.setMatchSummary("Spin twins Sajid Khan (10 wickets in match) and Noman Ali (9 wickets in match) decimated England's batting lineup on a turning Rawalpindi pitch after Saud Shakeel's composed 134 in the first innings, sealing a celebrated 2-1 series comeback for Pakistan.");
        resMatch2 = matchRepository.save(resMatch2);

        Innings engM2Inn1 = new Innings(resMatch2, 1, eng, pak);
        engM2Inn1.setTotalRuns(267);
        engM2Inn1.setWickets(10);
        engM2Inn1.setLegalBalls(410);
        engM2Inn1.setIsCompleted(true);
        engM2Inn1 = inningsRepository.save(engM2Inn1);
        saveBatting(engM2Inn1, root, 1, 88, 134, 10, 0, true, WicketType.LBW, sajid, null, "lbw b Sajid Khan");
        saveBowling(engM2Inn1, sajid, 138, 1, 128, 6);

        Innings pakM2Inn2 = new Innings(resMatch2, 2, pak, eng);
        pakM2Inn2.setTotalRuns(37);
        pakM2Inn2.setWickets(1);
        pakM2Inn2.setLegalBalls(20);
        pakM2Inn2.setTargetRuns(36);
        pakM2Inn2.setIsCompleted(true);
        pakM2Inn2 = inningsRepository.save(pakM2Inn2);
        saveBatting(pakM2Inn2, shakeel, 1, 134, 223, 5, 0, false, WicketType.NONE, null, null, "not out");
        saveBowling(pakM2Inn2, archer, 12, 0, 15, 0);

        log.info("CricPulse database successfully populated with realistic 2024 international cricket matches!");
    }

    private Player createPlayer(String name, Team team, PlayerRole role, String batStyle, String bowlStyle,
                                int jersey, int matches, int runs, int high, double avg, double sr,
                                int wickets, double bowlAvg, String bestBowl, double econ) {
        Player p = new Player(name, team, role, batStyle, bowlStyle, jersey);
        p.setCareerMatches(matches);
        p.setCareerRuns(runs);
        p.setCareerHighestScore(high);
        p.setCareerBattingAverage(avg);
        p.setCareerStrikeRate(sr);
        p.setCareerWickets(wickets);
        p.setCareerBowlingAverage(bowlAvg);
        p.setCareerBestBowling(bestBowl);
        p.setCareerEconomy(econ);
        return playerRepository.save(p);
    }

    private void saveBatting(Innings inn, Player player, int order, int runs, int balls, int fours, int sixes,
                             boolean isOut, WicketType type, Player bowler, Player fielder, String text) {
        BattingScore bs = new BattingScore(inn, player, order);
        bs.setRuns(runs);
        bs.setBallsFaced(balls);
        bs.setFours(fours);
        bs.setSixes(sixes);
        bs.setIsOut(isOut);
        bs.setWicketType(type);
        bs.setBowler(bowler);
        bs.setFielder(fielder);
        bs.setDismissalText(text);
        bs.calculateStrikeRate();
        battingScoreRepository.save(bs);
    }

    private void saveBowling(Innings inn, Player player, int legalBalls, int maidens, int runs, int wickets) {
        BowlingScore bs = new BowlingScore(inn, player);
        bs.setLegalBallsBowled(legalBalls);
        bs.setMaidens(maidens);
        bs.setRunsConceded(runs);
        bs.setWickets(wickets);
        bs.calculateEconomy();
        bowlingScoreRepository.save(bs);
    }

    private void saveFow(Innings inn, int num, int score, String overs, Player player) {
        FallOfWicket fow = new FallOfWicket(inn, num, score, overs, player);
        fallOfWicketRepository.save(fow);
    }

    private void saveDelivery(Innings inn, int overNum, int ballNum, int legalBall,
                              Player striker, Player nonStriker, Player bowler,
                              int batterRuns, int extraRuns, ExtraType extraType,
                              boolean isLegal, boolean isWicket, WicketType wicketType,
                              Player dismissed, Player fielder, String comm,
                              int teamRuns, int wickets, String oversFormatted, int seq) {
        Delivery d = new Delivery();
        d.setInnings(inn);
        d.setOverNumber(overNum);
        d.setBallNumberInOver(ballNum);
        d.setLegalBallNumber(legalBall);
        d.setStriker(striker);
        d.setNonStriker(nonStriker);
        d.setBowler(bowler);
        d.setRunsBatter(batterRuns);
        d.setRunsExtra(extraRuns);
        d.setExtraType(extraType);
        d.setIsLegalDelivery(isLegal);
        d.setIsWicket(isWicket);
        d.setWicketType(wicketType != null ? wicketType : WicketType.NONE);
        d.setDismissedPlayer(dismissed);
        d.setFielder(fielder);
        d.setCommentary(comm);
        d.setTeamRunsAtBall(teamRuns);
        d.setWicketsAtBall(wickets);
        d.setOversAtBallFormatted(oversFormatted);
        d.setSequenceOrder(seq);
        deliveryRepository.save(d);
    }

    private Team getOrCreateTeam(String name, String shortName, String flagUrl, String primaryColor) {
        return teamRepository.findByShortNameIgnoreCase(shortName)
                .map(t -> {
                    t.setName(name);
                    t.setLogoUrl(flagUrl);
                    t.setPrimaryColor(primaryColor);
                    return teamRepository.save(t);
                })
                .orElseGet(() -> teamRepository.save(new Team(name, shortName, flagUrl, primaryColor)));
    }
}
