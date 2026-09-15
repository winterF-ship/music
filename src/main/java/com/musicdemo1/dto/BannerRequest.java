package com.musicdemo1.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BannerRequest {
    @NotBlank(message = "轮播图标题不能为空")
    @Size(max = 150, message = "标题不能超过 150 个字符")
    private String title;
    @NotBlank(message = "请先上传轮播图图片")
    @Size(max = 255, message = "图片地址不能超过 255 个字符")
    private String imageUrl;
    @Size(max = 255, message = "跳转地址不能超过 255 个字符")
    private String targetUrl;
    @Min(value = 0, message = "排序值不能为负数")
    private Integer sortNo;
    @NotNull(message = "轮播图状态不能为空")
    @Min(value = 0, message = "状态只能为 0 或 1")
    @Max(value = 1, message = "状态只能为 0 或 1")
    private Integer status;
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
