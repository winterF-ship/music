package com.musicdemo1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UserStatusRequest {
    @NotNull(message = "账号状态不能为空")
    @Min(value = 0, message = "账号状态只能为 0 或 1")
    @Max(value = 1, message = "账号状态只能为 0 或 1")
    private Integer status;
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
