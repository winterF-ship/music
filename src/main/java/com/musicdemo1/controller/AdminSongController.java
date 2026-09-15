package com.musicdemo1.controller;

import com.musicdemo1.common.ApiResponse;
import com.musicdemo1.dto.FileUploadResponse;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.dto.SongRequest;
import com.musicdemo1.dto.SongView;
import com.musicdemo1.service.FileStorageService;
import com.musicdemo1.service.SongManagementService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/songs")
public class AdminSongController {
    private final SongManagementService songManagementService;
    private final FileStorageService fileStorageService;

    public AdminSongController(SongManagementService songManagementService, FileStorageService fileStorageService) {
        this.songManagementService = songManagementService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<PageResponse<SongView>> page(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long singerId) {
        return ApiResponse.ok(songManagementService.page(page, size, keyword, singerId));
    }

    @GetMapping("/{id}")
    public ApiResponse<SongView> get(@PathVariable Long id) { return ApiResponse.ok(songManagementService.get(id)); }

    @PostMapping
    public ApiResponse<SongView> create(@Valid @RequestBody SongRequest request) { return ApiResponse.ok(songManagementService.create(request)); }

    @PutMapping("/{id}")
    public ApiResponse<SongView> update(@PathVariable Long id, @Valid @RequestBody SongRequest request) { return ApiResponse.ok(songManagementService.update(id, request)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { songManagementService.delete(id); return ApiResponse.ok(); }

    @PostMapping(value = "/upload/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> uploadAudio(@RequestParam("audio") MultipartFile audio) {
        return ApiResponse.ok(fileStorageService.storeAudio(audio));
    }

    @PostMapping(value = "/upload/lyrics", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> uploadLyrics(@RequestParam("lyrics") MultipartFile lyrics) {
        return ApiResponse.ok(fileStorageService.storeLyrics(lyrics));
    }
}
