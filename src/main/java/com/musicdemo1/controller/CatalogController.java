package com.musicdemo1.controller;

import com.musicdemo1.common.ApiResponse;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.dto.PlaylistDetailResponse;
import com.musicdemo1.dto.SingerDetailResponse;
import com.musicdemo1.dto.SongView;
import com.musicdemo1.entity.Singer;
import com.musicdemo1.entity.SongList;
import com.musicdemo1.entity.Banner;
import java.util.List;
import com.musicdemo1.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/songs")
    public ApiResponse<PageResponse<SongView>> songs(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long singerId) {
        return ApiResponse.ok(catalogService.songs(page, size, keyword, singerId));
    }

    @GetMapping("/songs/{id}")
    public ApiResponse<SongView> song(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.song(id));
    }

    @GetMapping("/singers")
    public ApiResponse<PageResponse<Singer>> singers(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(catalogService.singers(page, size));
    }

    @GetMapping("/singers/{id}")
    public ApiResponse<SingerDetailResponse> singer(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.singer(id));
    }

    @GetMapping("/playlists")
    public ApiResponse<PageResponse<SongList>> playlists(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(catalogService.playlists(page, size));
    }

    @GetMapping("/playlists/{id}")
    public ApiResponse<PlaylistDetailResponse> playlist(@PathVariable Long id) {
        return ApiResponse.ok(catalogService.playlist(id));
    }

    @GetMapping("/banners")
    public ApiResponse<List<Banner>> banners() {
        return ApiResponse.ok(catalogService.banners());
    }
}
