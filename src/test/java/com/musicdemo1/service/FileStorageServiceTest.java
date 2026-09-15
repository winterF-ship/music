package com.musicdemo1.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class FileStorageServiceTest {
    @Test
    void storesExtensionlessJpegBlobUsingItsContentType(@TempDir Path uploadDir) {
        FileStorageService service = new FileStorageService(uploadDir.toString());
        MockMultipartFile blob = new MockMultipartFile("image", "blob", "image/jpeg", new byte[] { 1, 2, 3 });

        String url = service.storeImage(blob).url();

        assertTrue(url.matches("/uploads/images/[0-9a-f-]+\\.jpg"));
    }
}
