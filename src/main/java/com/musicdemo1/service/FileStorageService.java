package com.musicdemo1.service;

import com.musicdemo1.common.BusinessException;
import com.musicdemo1.dto.FileUploadResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "wav", "ogg", "m4a", "aac");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> LYRIC_EXTENSIONS = Set.of("lrc");
    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public FileUploadResponse storeAudio(MultipartFile file) {
        return store(file, AUDIO_EXTENSIONS, "audio", 50 * 1024 * 1024, "音频");
    }

    public FileUploadResponse storeImage(MultipartFile file) {
        return store(file, IMAGE_EXTENSIONS, "images", 10 * 1024 * 1024, "图片");
    }

    public FileUploadResponse storeLyrics(MultipartFile file) {
        return store(file, LYRIC_EXTENSIONS, "lyrics", 2 * 1024 * 1024, "歌词");
    }

    private FileUploadResponse store(MultipartFile file, Set<String> allowedExtensions, String folder,
            long maxSize, String mediaName) {
        if (file == null || file.isEmpty()) throw new BusinessException("请选择" + mediaName + "文件");
        if (file.getSize() > maxSize) throw new BusinessException(mediaName + "文件过大");
        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        String extension = extension(originalName);
        if (extension.isBlank()) extension = extensionFromContentType(contentType, allowedExtensions);
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException("不支持的" + mediaName + "格式");
        }
        String expectedPrefix = "audio".equals(folder) ? "audio/" : "images".equals(folder) ? "image/" : "text/";
        if (contentType != null && !contentType.startsWith(expectedPrefix) && !"application/octet-stream".equals(contentType)) {
            throw new BusinessException("上传文件不是有效" + mediaName + "类型");
        }
        Path directory = uploadRoot.resolve(folder).normalize();
        Path destination = directory.resolve(UUID.randomUUID() + "." + extension).normalize();
        if (!destination.startsWith(directory)) throw new BusinessException("文件名不合法");
        try {
            Files.createDirectories(directory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destination);
            }
        } catch (IOException exception) {
            throw new BusinessException(500, mediaName + "保存失败，请稍后重试");
        }
        return new FileUploadResponse("/uploads/" + folder + "/" + destination.getFileName(), originalName, file.getSize());
    }

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String extensionFromContentType(String contentType, Set<String> allowedExtensions) {
        if (contentType == null) return "";
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> allowedExtensions.contains("jpg") ? "jpg" : "jpeg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "audio/mpeg" -> "mp3";
            case "audio/wav", "audio/x-wav" -> "wav";
            case "audio/ogg" -> "ogg";
            case "audio/mp4", "audio/x-m4a" -> "m4a";
            case "audio/aac" -> "aac";
            case "text/plain" -> "lrc";
            default -> "";
        };
    }
}
