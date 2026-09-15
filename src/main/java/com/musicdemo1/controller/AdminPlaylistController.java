package com.musicdemo1.controller;

import com.musicdemo1.common.ApiResponse;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.dto.PlaylistDetailResponse;
import com.musicdemo1.dto.PlaylistSongRequest;
import com.musicdemo1.dto.SongListRequest;
import com.musicdemo1.entity.SongList;
import com.musicdemo1.service.PlaylistManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/playlists")
public class AdminPlaylistController {
    private final PlaylistManagementService playlistManagementService;

    public AdminPlaylistController(PlaylistManagementService playlistManagementService) {
        this.playlistManagementService = playlistManagementService;
    }

    @GetMapping
    public ApiResponse<PageResponse<SongList>> page(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size, @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(playlistManagementService.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlaylistDetailResponse> get(@PathVariable Long id) { return ApiResponse.ok(playlistManagementService.get(id)); }

    @PostMapping
    public ApiResponse<SongList> create(@Valid @RequestBody SongListRequest request) { return ApiResponse.ok(playlistManagementService.create(request)); }

    @PutMapping("/{id}")
    public ApiResponse<SongList> update(@PathVariable Long id, @Valid @RequestBody SongListRequest request) { return ApiResponse.ok(playlistManagementService.update(id, request)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { playlistManagementService.delete(id); return ApiResponse.ok(); }

    @PostMapping("/{id}/songs")
    public ApiResponse<PlaylistDetailResponse> addSong(@PathVariable Long id, @Valid @RequestBody PlaylistSongRequest request) {
        return ApiResponse.ok(playlistManagementService.addSong(id, request));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ApiResponse<PlaylistDetailResponse> removeSong(@PathVariable Long id, @PathVariable Long songId) {
        return ApiResponse.ok(playlistManagementService.removeSong(id, songId));
    }
}
