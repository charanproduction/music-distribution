//package com.digital.service;
//
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Service;
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//@Primary
//public class HuggingFaceAiPosterService implements AiPosterService {
//
//    @Value("${hf.api.key}")
//    private String apiKey;
//
//    private static final String MODEL_URL =
//            "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-xl-base-1.0";
//
//    @Override
//    public byte[] generatePoster(String prompt) throws Exception {
//        HttpURLConnection conn = (HttpURLConnection) new URL(MODEL_URL).openConnection();
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
//        conn.setRequestProperty("Content-Type", "application/json");
//        conn.setDoOutput(true);
//
//        String body = "{\"inputs\":\"" + prompt + "\"}";
//        conn.getOutputStream().write(body.getBytes());
//
//        return conn.getInputStream().readAllBytes();
//    }
//}
