package com.musicdemo1.controller;

import com.musicdemo1.common.ApiResponse;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.dto.SingerRequest;
import com.musicdemo1.entity.Singer;
import com.musicdemo1.service.SingerManagementService;
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
@RequestMapping("/api/admin/singers")
public class AdminSingerController {
    private final SingerManagementService singerManagementService;

    public AdminSingerController(SingerManagementService singerManagementService) {
        this.singerManagementService = singerManagementService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Singer>> page(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(singerManagementService.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<Singer> get(@PathVariable Long id) { return ApiResponse.ok(singerManagementService.get(id)); }

    @PostMapping
    public ApiResponse<Singer> create(@Valid @RequestBody SingerRequest request) {
        return ApiResponse.ok(singerManagementService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Singer> update(@PathVariable Long id, @Valid @RequestBody SingerRequest request) {
        return ApiResponse.ok(singerManagementService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        singerManagementService.delete(id);
        return ApiResponse.ok();
    }
}
