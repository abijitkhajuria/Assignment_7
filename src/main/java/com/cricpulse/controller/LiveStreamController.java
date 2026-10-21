package com.cricpulse.controller;

import com.cricpulse.dto.LiveMatchSnapshotDto;
import com.cricpulse.service.ScoringEngineService;
import com.cricpulse.service.SseBroadcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Live Stream", description = "Server-Sent Events (SSE) streaming APIs for real-time match scores")
public class LiveStreamController {

    private final SseBroadcastService sseBroadcastService;
    private final ScoringEngineService scoringEngineService;

    public LiveStreamController(SseBroadcastService sseBroadcastService, ScoringEngineService scoringEngineService) {
        this.sseBroadcastService = sseBroadcastService;
        this.scoringEngineService = scoringEngineService;
    }

    @GetMapping(value = "/{id}/live-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to live real-time score events via Server-Sent Events (SSE)")
    public SseEmitter subscribeToMatchLiveStream(@PathVariable Long id) {
        SseEmitter emitter = sseBroadcastService.subscribe(id);

        // Immediately send current live snapshot so client has full state on initial connect
        try {
            LiveMatchSnapshotDto snapshot = scoringEngineService.buildLiveMatchSnapshot(id);
            emitter.send(SseEmitter.event()
                    .name("SCORE_UPDATE")
                    .data(snapshot));
        } catch (IOException ignored) {}

        return emitter;
    }
}
