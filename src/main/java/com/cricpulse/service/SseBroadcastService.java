package com.cricpulse.service;

import com.cricpulse.dto.LiveMatchSnapshotDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(SseBroadcastService.class);
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long matchId) {
        // 30-minute timeout for SSE stream
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.computeIfAbsent(matchId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(matchId, emitter));
        emitter.onTimeout(() -> removeEmitter(matchId, emitter));
        emitter.onError(e -> removeEmitter(matchId, emitter));

        try {
            // Send initial ping to establish connection
            emitter.send(SseEmitter.event().name("INIT").data("Connected to CricPulse Live Score Stream for Match #" + matchId));
        } catch (IOException e) {
            removeEmitter(matchId, emitter);
        }

        return emitter;
    }

    public void broadcast(Long matchId, LiveMatchSnapshotDto snapshot) {
        List<SseEmitter> matchEmitters = emitters.get(matchId);
        if (matchEmitters == null || matchEmitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : matchEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("SCORE_UPDATE")
                        .data(snapshot));
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        matchEmitters.removeAll(deadEmitters);
    }

    private void removeEmitter(Long matchId, SseEmitter emitter) {
        List<SseEmitter> matchEmitters = emitters.get(matchId);
        if (matchEmitters != null) {
            matchEmitters.remove(emitter);
            if (matchEmitters.isEmpty()) {
                emitters.remove(matchId);
            }
        }
    }
}
