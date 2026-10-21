package com.cricpulse.controller;

import com.cricpulse.dto.LiveMatchSnapshotDto;
import com.cricpulse.dto.MatchSummaryDto;
import com.cricpulse.dto.ScorecardResponseDto;
import com.cricpulse.entity.MatchFormat;
import com.cricpulse.service.MatchService;
import com.cricpulse.service.ScoringEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "Cricket match schedules, live games, and scorecards")
public class MatchController {

    private final MatchService matchService;
    private final ScoringEngineService scoringEngineService;

    public MatchController(MatchService matchService, ScoringEngineService scoringEngineService) {
        this.matchService = matchService;
        this.scoringEngineService = scoringEngineService;
    }

    @GetMapping
    @Operation(summary = "Get all matches with optional format filter (T20, ODI, TEST)")
    public ResponseEntity<List<MatchSummaryDto>> getAllMatches(@RequestParam(required = false) MatchFormat format) {
        return ResponseEntity.ok(matchService.getAllMatches(format));
    }

    @GetMapping("/live")
    @Operation(summary = "Get all currently live matches (including innings break)")
    public ResponseEntity<List<MatchSummaryDto>> getLiveMatches() {
        return ResponseEntity.ok(matchService.getLiveMatches());
    }

    @GetMapping("/today")
    @Operation(summary = "Get all matches taking place today (live, scheduled or completed today)")
    public ResponseEntity<List<MatchSummaryDto>> getTodayMatches() {
        return ResponseEntity.ok(matchService.getTodayMatches());
    }

    @GetMapping("/tomorrow")
    @Operation(summary = "Get all matches scheduled for tomorrow")
    public ResponseEntity<List<MatchSummaryDto>> getTomorrowMatches() {
        return ResponseEntity.ok(matchService.getTomorrowMatches());
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get all upcoming scheduled matches")
    public ResponseEntity<List<MatchSummaryDto>> getUpcomingMatches() {
        return ResponseEntity.ok(matchService.getUpcomingMatches());
    }

    @GetMapping("/completed")
    @Operation(summary = "Get all completed match results with optional format filter")
    public ResponseEntity<List<MatchSummaryDto>> getCompletedMatches(@RequestParam(required = false) MatchFormat format) {
        return ResponseEntity.ok(matchService.getCompletedMatches(format));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single match summary by match ID")
    public ResponseEntity<MatchSummaryDto> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchSummary(id));
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get match summary for widget or banner display")
    public ResponseEntity<MatchSummaryDto> getMatchSummary(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchSummary(id));
    }

    @GetMapping("/{id}/scorecard")
    @Operation(summary = "Get complete full scorecard with 1st & 2nd innings details")
    public ResponseEntity<ScorecardResponseDto> getScorecard(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getScorecard(id));
    }

    @GetMapping("/{id}/live-snapshot")
    @Operation(summary = "Get real-time snapshot of live match (used by SSE & polling)")
    public ResponseEntity<LiveMatchSnapshotDto> getLiveSnapshot(@PathVariable Long id) {
        return ResponseEntity.ok(scoringEngineService.buildLiveMatchSnapshot(id));
    }
}
