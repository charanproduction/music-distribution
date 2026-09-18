//package com.digital.service;
//
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import com.openai.client.OpenAIClient;
//import com.openai.models.responses.Response;
//import com.openai.models.responses.ResponseCreateParams;
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Base64;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class OpenAIPosterService {
//
//    private final OpenAIClient openAIClient;
//
//    /**
//     * Translate & stylize song title
//     */
//   
//
//    /**
//     * Create poster from prompt (GPT-Image-1)
//     */
//    public byte[] generatePoster(String prompt) {
//
//        ResponseCreateParams params = ResponseCreateParams.builder()
//                .model("gpt-image-1")
//                .input(prompt)
//                .build();
//
//        Response response = openAIClient.responses().create(params);
//
//        // Extract base64 image
//        String base64Image = response.output().get(0)
//                .content().get(0)
//                .image().get(0)
//                .data();
//
//        return java.util.Base64.getDecoder().decode(base64Image);
//    }
//
//    /**
//     * Combine image inputs into the prompt (GPT-image does not accept image files directly yet)
//     */
//    public String buildPromptWithImages(String basePrompt, Path singer, Path logo) throws Exception {
//
//        String singerB64 = Base64.getEncoder().encodeToString(Files.readAllBytes(singer));
//        String logoB64   = Base64.getEncoder().encodeToString(Files.readAllBytes(logo));
//
//        return basePrompt + "\n\n" +
//                "Singer Image (base64): " + singerB64 + "\n\n" +
//                "Company Logo (base64): " + logoB64 + "\n";
//    }
//}
