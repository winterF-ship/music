package com.musicdemo1.controller;

import com.musicdemo1.common.ApiResponse;
import com.musicdemo1.dto.BannerRequest;
import com.musicdemo1.dto.FileUploadResponse;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.entity.Banner;
import com.musicdemo1.service.BannerManagementService;
import com.musicdemo1.service.FileStorageService;
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
@RequestMapping("/api/admin/banners")
public class AdminBannerController {
    private final BannerManagementService bannerManagementService;
    private final FileStorageService fileStorageService;

    public AdminBannerController(BannerManagementService bannerManagementService, FileStorageService fileStorageService) {
        this.bannerManagementService = bannerManagementService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Banner>> page(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) { return ApiResponse.ok(bannerManagementService.page(page, size, keyword)); }

    @GetMapping("/{id}")
    public ApiResponse<Banner> get(@PathVariable Long id) { return ApiResponse.ok(bannerManagementService.get(id)); }

    @PostMapping
    public ApiResponse<Banner> create(@Valid @RequestBody BannerRequest request) { return ApiResponse.ok(bannerManagementService.create(request)); }

    @PutMapping("/{id}")
    public ApiResponse<Banner> update(@PathVariable Long id, @Valid @RequestBody BannerRequest request) { return ApiResponse.ok(bannerManagementService.update(id, request)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { bannerManagementService.delete(id); return ApiResponse.ok(); }

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> uploadImage(@RequestParam("image") MultipartFile image) { return ApiResponse.ok(fileStorageService.storeImage(image)); }
}
