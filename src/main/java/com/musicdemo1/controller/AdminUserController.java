package com.musicdemo1.controller;

import com.musicdemo1.common.ApiResponse;
import com.musicdemo1.dto.PageResponse;
import com.musicdemo1.dto.UserStatusRequest;
import com.musicdemo1.dto.UserUpdateRequest;
import com.musicdemo1.dto.UserView;
import com.musicdemo1.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserManagementService userManagementService;

    public AdminUserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserView>> page(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(userManagementService.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserView> get(@PathVariable Long id) {
        return ApiResponse.ok(userManagementService.get(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserView> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok(userManagementService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UserView> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        return ApiResponse.ok(userManagementService.updateStatus(id, request.getStatus()));
    }


    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userManagementService.delete(id);
        return ApiResponse.ok();
    }
}
