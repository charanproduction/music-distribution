package com.digital.service;

public interface AiPosterService {
    byte[] generatePoster(String prompt) throws Exception;
    default String buildPrompt(String title, String lang) {
        return "Professional album cover poster, vibrant colors, music theme, text: "
                + title + " language=" + lang + ", cinematic lighting, HD, 1:1 aspect";
    }
}
