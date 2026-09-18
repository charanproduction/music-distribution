package com.digital.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${posters.upload-root:uploads}")
    private String uploadRoot;

    /**
     * Normalize upload root directory
     */
    private Path root() {
        return Paths.get(uploadRoot).toAbsolutePath().normalize();
    }

    /**
     * Convert to safe folder/file name
     */
    private String slug(String value) {
        if (value == null) return "untitled";
        return value.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    /**
     * Ensure a directory exists
     */
    private Path ensureDir(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    /**
     * Save artist / channel / label asset images
     * Now stored under:
     * uploads/{username}/{song-slug}/assets/
     */
    public Path saveAsset(MultipartFile file,
                          String username,
                          String songTitle,
                          String suffix) throws IOException {

        String userSlug = slug(username);
        String songSlug = slug(songTitle);

        Path assetDir = ensureDir(
                root().resolve(userSlug)
                      .resolve(songSlug)
                      .resolve("assets")
        );

        String ext = getExtension(file.getOriginalFilename());
        String filename = suffix + ext;  // cleaner: artist.jpg, channel-logo.png

        Path target = assetDir.resolve(filename);

        file.transferTo(target);
        return target;
    }

    /**
     * Save final poster image
     * Stored under:
     * uploads/{username}/{song-slug}/posters/
     *
     * Filename:
     *   {song-title-slug}-{timestamp}.jpg
     */
    public Path savePosterImage(byte[] imageBytes,
                                String username,
                                String songTitle) throws IOException {

        String userSlug = slug(username);
        String songSlug = slug(songTitle);

        Path posterDir = ensureDir(
                root().resolve(userSlug)
                      .resolve(songSlug)
                      .resolve("posters")
        );

        String ts = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String filename = songSlug + "-" + ts + ".jpg";

        Path target = posterDir.resolve(filename);
        Files.write(target, imageBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return target;
    }

    /**
     * Extract extension from filename
     */
    private String getExtension(String original) {
        if (original == null) return ".jpg";
        int idx = original.lastIndexOf('.');
        if (idx == -1) return ".jpg";
        return original.substring(idx).toLowerCase();
    }
}
