package com.musicdemo1.controller;

import com.musicdemo1.common.ApiResponse;
import com.musicdemo1.dto.FavoriteStateResponse;
import com.musicdemo1.dto.FileUploadResponse;
import com.musicdemo1.dto.ListenRecordRequest;
import com.musicdemo1.dto.ListenSummaryResponse;
import com.musicdemo1.dto.PlaylistDetailResponse;
import com.musicdemo1.dto.PlaylistSongRequest;
import com.musicdemo1.dto.SongListRequest;
import com.musicdemo1.dto.SongView;
import com.musicdemo1.dto.UserProfileUpdateRequest;
import com.musicdemo1.dto.UserView;
import com.musicdemo1.entity.SongList;
import com.musicdemo1.service.FileStorageService;
import com.musicdemo1.service.UserAccountService;
import com.musicdemo1.service.UserFavoriteService;
import com.musicdemo1.service.UserListenService;
import com.musicdemo1.service.UserPlaylistService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/user")
public class UserController {
    private final UserAccountService accountService;
    private final UserFavoriteService favoriteService;
    private final UserPlaylistService playlistService;
    private final UserListenService listenService;
    private final FileStorageService fileStorageService;

    public UserController(UserAccountService accountService, UserFavoriteService favoriteService,
            UserPlaylistService playlistService, UserListenService listenService,
            FileStorageService fileStorageService) {
        this.accountService = accountService;
        this.favoriteService = favoriteService;
        this.playlistService = playlistService;
        this.listenService = listenService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserView> profile(Authentication authentication) {
        return ApiResponse.ok(accountService.profile(userId(authentication)));
    }

    @PutMapping("/profile")
    public ApiResponse<UserView> updateProfile(Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.ok(accountService.update(userId(authentication), request));
    }

    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserView> uploadAvatar(Authentication authentication,
            @RequestParam("avatar") MultipartFile avatar) {
        String avatarUrl = fileStorageService.storeImage(avatar).url();
        return ApiResponse.ok(accountService.updateAvatar(userId(authentication), avatarUrl));
    }

    @GetMapping("/favorites/songs")
    public ApiResponse<List<SongView>> favoriteSongs(Authentication authentication) {
        return ApiResponse.ok(favoriteService.songs(userId(authentication)));
    }

    @PostMapping("/favorites/songs/{songId}")
    public ApiResponse<FavoriteStateResponse> addFavoriteSong(Authentication authentication,
            @PathVariable Long songId) {
        return ApiResponse.ok(favoriteService.addSong(userId(authentication), songId));
    }

    @DeleteMapping("/favorites/songs/{songId}")
    public ApiResponse<FavoriteStateResponse> removeFavoriteSong(Authentication authentication,
            @PathVariable Long songId) {
        return ApiResponse.ok(favoriteService.removeSong(userId(authentication), songId));
    }

    @GetMapping("/favorites/playlists")
    public ApiResponse<List<SongList>> favoritePlaylists(Authentication authentication) {
        return ApiResponse.ok(favoriteService.playlists(userId(authentication)));
    }

    @PostMapping("/favorites/playlists/{playlistId}")
    public ApiResponse<FavoriteStateResponse> addFavoritePlaylist(Authentication authentication,
            @PathVariable Long playlistId) {
        return ApiResponse.ok(favoriteService.addPlaylist(userId(authentication), playlistId));
    }

    @DeleteMapping("/favorites/playlists/{playlistId}")
    public ApiResponse<FavoriteStateResponse> removeFavoritePlaylist(Authentication authentication,
            @PathVariable Long playlistId) {
        return ApiResponse.ok(favoriteService.removePlaylist(userId(authentication), playlistId));
    }

    @PostMapping("/listen-records")
    public ApiResponse<Void> recordListen(Authentication authentication,
            @Valid @RequestBody ListenRecordRequest request) {
        listenService.record(userId(authentication), request.sessionId(), request.songId(), request.seconds());
        return ApiResponse.ok(null);
    }

    @GetMapping("/listen-records/summary")
    public ApiResponse<ListenSummaryResponse> listenSummary(Authentication authentication) {
        return ApiResponse.ok(new ListenSummaryResponse(listenService.totalSeconds(userId(authentication))));
    }

    @GetMapping("/playlists")
    public ApiResponse<List<SongList>> playlists(Authentication authentication) {
        return ApiResponse.ok(playlistService.list(userId(authentication)));
    }

    @PostMapping("/playlists")
    public ApiResponse<SongList> createPlaylist(Authentication authentication,
            @Valid @RequestBody SongListRequest request) {
        return ApiResponse.ok(playlistService.create(userId(authentication), request));
    }

    @PutMapping("/playlists/{playlistId}")
    public ApiResponse<SongList> updatePlaylist(Authentication authentication,
            @PathVariable Long playlistId, @Valid @RequestBody SongListRequest request) {
        return ApiResponse.ok(playlistService.update(userId(authentication), playlistId, request));
    }

    @PostMapping(value = "/playlists/upload/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> uploadPlaylistCover(Authentication authentication,
            @RequestParam("cover") MultipartFile cover) {
        userId(authentication);
        return ApiResponse.ok(fileStorageService.storeImage(cover));
    }

    @PostMapping("/playlists/{playlistId}/songs")
    public ApiResponse<PlaylistDetailResponse> addSong(Authentication authentication,
            @PathVariable Long playlistId, @Valid @RequestBody PlaylistSongRequest request) {
        return ApiResponse.ok(playlistService.addSong(userId(authentication), playlistId, request));
    }

    private Long userId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}
