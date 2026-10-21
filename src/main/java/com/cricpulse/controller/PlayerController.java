package com.cricpulse.controller;

import com.cricpulse.dto.PlayerDto;
import com.cricpulse.dto.PlayerStatsDto;
import com.cricpulse.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Players", description = "Cricket player profiles and statistics APIs")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    @Operation(summary = "Get all players, optionally filtered by team ID")
    public ResponseEntity<List<PlayerDto>> getAllPlayers(@RequestParam(required = false) Long teamId) {
        return ResponseEntity.ok(playerService.getAllPlayers(teamId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player profile by ID")
    public ResponseEntity<PlayerDto> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "Get player career & tournament live recorded statistics")
    public ResponseEntity<PlayerStatsDto> getPlayerStatistics(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerStatistics(id));
    }
}
