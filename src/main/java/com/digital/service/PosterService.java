package com.digital.service;

import com.digital.entity.Poster;
import com.digital.entity.User;
import com.digital.repository.PosterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PosterService {

    private final PosterRepository posterRepository;
    private final FileStorageService fileStorageService;
    private final PosterAIService posterAIService;

    /**
     * Main method to generate a complete music poster using AI + uploaded assets
     */
    @Transactional
    public Poster generatePoster(User user,
                                 String songTitle,
                                 String language,
                                 String artistName,
                                 String channelName,
                                 String productionLabel,
                                 String userPrompt,
                                 MultipartFile artistImage,
                                 MultipartFile channelLogo,
                                 MultipartFile productionLogo) {

        try {
            String username = user.getUsername();

            // 1️⃣ Translate title into target language (Odia, etc.)
            String translatedTitle = posterAIService.translateToLanguage(songTitle, language);

            // 2️⃣ Build enhanced multimodal prompt
            String finalPrompt = buildFinalPosterPrompt(
                    songTitle,
                    translatedTitle,
                    language,
                    artistName,
                    channelName,
                    productionLabel,
                    userPrompt
            );

            // 3️⃣ Store user-uploaded images in local filesystem
            Path artistPath = null;
            Path channelPath = null;
            Path labelPath = null;

            if (artistImage != null && !artistImage.isEmpty()) {
                artistPath = fileStorageService.saveAsset(artistImage, username, songTitle, "artist");
            }
            if (channelLogo != null && !channelLogo.isEmpty()) {
                channelPath = fileStorageService.saveAsset(channelLogo, username, songTitle, "channel-logo");
            }
            if (productionLogo != null && !productionLogo.isEmpty()) {
                labelPath = fileStorageService.saveAsset(productionLogo, username, songTitle, "label-logo");
            }

            // 4️⃣ Generate AI-based poster using multimodal input (prompt + images)
            byte[] posterBytes = posterAIService.generateFinalPoster(
                    finalPrompt,
                    artistPath,
                    channelPath,
                    labelPath
            );

            // 5️⃣ Save AI poster file to user folder
            Path posterPath = fileStorageService.savePosterImage(posterBytes, username, songTitle);

            // 6️⃣ Persist DB entity
            Poster poster = Poster.builder()
                    .user(user)
                    .songTitle(songTitle)
                    .language(language)
                    .artistName(artistName)
                    .channelName(channelName)
                    .productionLabel(productionLabel)
                    .prompt(userPrompt)
                    .imagePath(posterPath.toString())
                    .aiGenerated(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            return posterRepository.save(poster);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate poster", e);
        }
    }


    /**
     * ✔ FIXED: This is the correct cinematic poster prompt.
     * ✔ Ensures Odia or any language text is correct Unicode
     * ✔ Ensures metadata appears exactly once
     * ✔ Prevents AI from repeating titles
     * ✔ Defines exact layout rules
     */
    private String buildFinalPosterPrompt(String songTitle,
                                          String translatedTitle,
                                          String language,
                                          String artistName,
                                          String channelName,
                                          String productionLabel,
                                          String userPrompt) {

        return """
                Create a cinematic digital music poster.

                STRICT RULES:

                1️⃣ MAIN ENGLISH TITLE:
                   "%s"

                2️⃣ TRANSLATED TITLE (%s):
                   "%s"
                   • Must use full correct Unicode script.
                   • Do NOT mix English and regional characters.
                   • Do NOT transliterate.

                3️⃣ POSTER LAYOUT:
                   • Artist photo centered.
                   • English title at top-center in bold cinematic font.
                   • Translated title beneath it, slightly smaller but prominent.
                   • Channel logo bottom-right.
                   • Production label logo bottom-left.
                   • Warm cinematic lighting (gold, orange, brown tones).
                   • DO NOT repeat titles anywhere else.

                4️⃣ METADATA (bottom-left small white text):
                   Artist: %s
                   Channel: %s
                   Label: %s

                5️⃣ DESIGN RULES:
                   • Do NOT distort uploaded images.
                   • Blend artist photo naturally with depth-of-field.
                   • Maintain clean typography.
                   • Suitable for YouTube / Spotify / Music Promotions.

                6️⃣ USER CREATIVE INPUT:
                   %s
                """
                .formatted(
                        songTitle,
                        language,
                        translatedTitle,
                        artistName,
                        channelName,
                        productionLabel,
                        (userPrompt == null ? "" : userPrompt)
                );
    }
}
