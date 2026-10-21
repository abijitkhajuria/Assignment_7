package com.cricpulse.controller;

import com.cricpulse.dto.AdminLoginRequestDto;
import com.cricpulse.dto.CreateMatchRequestDto;
import com.cricpulse.entity.CricketMatch;
import com.cricpulse.service.AdminService;
import com.cricpulse.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Operations", description = "Administrative controls for match creation and lifecycle")
public class AdminController {

    private final AdminService adminService;
    private final MatchService matchService;
    private final com.cricpulse.service.SeedDataService seedDataService;

    public AdminController(AdminService adminService, MatchService matchService, com.cricpulse.service.SeedDataService seedDataService) {
        this.adminService = adminService;
        this.matchService = matchService;
        this.seedDataService = seedDataService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate administrator credentials")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequestDto request) {
        return ResponseEntity.ok(adminService.authenticate(request));
    }

    @PostMapping("/reseed")
    @Operation(summary = "Reset and re-populate database with authentic modern international cricket fixtures")
    public ResponseEntity<Map<String, String>> reseedDatabase() {
        seedDataService.reseedAccurateFixtures();
        return ResponseEntity.ok(Map.of("message", "Database successfully re-seeded with realistic 2024 fixtures!"));
    }

    @PostMapping("/matches")
    @Operation(summary = "Create a new match schedule and initialize 1st innings")
    public ResponseEntity<CricketMatch> createMatch(@Valid @RequestBody CreateMatchRequestDto request) {
        CricketMatch match = matchService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(match);
    }

    @PostMapping("/matches/{id}/start")
    @Operation(summary = "Mark a scheduled match as LIVE")
    public ResponseEntity<CricketMatch> startMatch(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.startMatch(id));
    }

    @PostMapping("/matches/{id}/finish")
    @Operation(summary = "Declare match complete with optional winner and result margin")
    public ResponseEntity<CricketMatch> finishMatch(
            @PathVariable Long id,
            @RequestParam(required = false) String resultMargin,
            @RequestParam(required = false) Long winnerTeamId) {
        return ResponseEntity.ok(adminService.finishMatch(id, resultMargin, winnerTeamId));
    }

    @DeleteMapping("/matches/{id}")
    @Operation(summary = "Delete a match record by ID")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        adminService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }
}
