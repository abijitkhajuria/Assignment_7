package com.cricpulse.controller;

import com.cricpulse.dto.DeliveryRequestDto;
import com.cricpulse.entity.Delivery;
import com.cricpulse.entity.Innings;
import com.cricpulse.repository.DeliveryRepository;
import com.cricpulse.repository.InningsRepository;
import com.cricpulse.service.ScoringEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Scoring Engine", description = "Live cricket ball-by-ball scoring and innings control APIs")
public class ScoringController {

    private final ScoringEngineService scoringEngineService;
    private final InningsRepository inningsRepository;
    private final DeliveryRepository deliveryRepository;

    public ScoringController(
            ScoringEngineService scoringEngineService,
            InningsRepository inningsRepository,
            DeliveryRepository deliveryRepository) {
        this.scoringEngineService = scoringEngineService;
        this.inningsRepository = inningsRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @GetMapping("/matches/{id}/innings")
    @Operation(summary = "Get list of innings for a specific match")
    public ResponseEntity<List<Innings>> getMatchInnings(@PathVariable Long id) {
        return ResponseEntity.ok(inningsRepository.findByMatchIdOrderByInningsNumberAsc(id));
    }

    @GetMapping("/innings/{id}/deliveries")
    @Operation(summary = "Get all recorded delivery events for an innings in chronological order")
    public ResponseEntity<List<Delivery>> getInningsDeliveries(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryRepository.findByInningsIdOrderBySequenceOrderAsc(id));
    }

    @PostMapping("/innings/{id}/deliveries")
    @Operation(summary = "Record a single delivery event (runs, wickets, extras, strike rotation)")
    public ResponseEntity<Delivery> recordDelivery(
            @PathVariable Long id,
            @Valid @RequestBody DeliveryRequestDto request) {
        Delivery delivery = scoringEngineService.recordDelivery(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(delivery);
    }

    @PostMapping("/innings/{id}/undo-last-delivery")
    @Operation(summary = "Undo the most recent delivery, rolling back batter/bowler figures and match state")
    public ResponseEntity<Map<String, Object>> undoLastDelivery(@PathVariable Long id) {
        scoringEngineService.undoLastDelivery(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Last delivery successfully undone and match state restored."
        ));
    }

    @PostMapping("/innings/{id}/complete")
    @Operation(summary = "Officially complete an innings and initialize target or finish match")
    public ResponseEntity<Map<String, Object>> completeInnings(@PathVariable Long id) {
        scoringEngineService.completeInnings(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Innings successfully marked as completed."
        ));
    }
}
