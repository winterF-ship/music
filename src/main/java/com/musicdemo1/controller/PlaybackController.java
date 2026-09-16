package com.musicdemo1.controller;

import com.musicdemo1.common.ApiResponse;
import com.musicdemo1.dto.CountItem;
import com.musicdemo1.service.PlaybackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
public class PlaybackController {
    private final PlaybackService playback;
    public PlaybackController(PlaybackService playback) { this.playback = playback; }
    public record PlayRequest(@NotNull UUID eventId) {}

    @PostMapping("/api/songs/{id}/plays")
    public ApiResponse<Void> record(@PathVariable long id, @Valid @RequestBody PlayRequest request) {
        playback.record(id, request.eventId());
        return ApiResponse.ok(null);
    }

    @GetMapping("/api/admin/analytics/song-plays")
    public ApiResponse<List<CountItem>> counts() { return ApiResponse.ok(playback.counts()); }
}
