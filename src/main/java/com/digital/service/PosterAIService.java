package com.digital.service;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.images.ImageGenerateParams;
import com.openai.models.images.ImageModel;
import com.openai.models.images.ImagesResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PosterAIService {

    private final OpenAIClient openAIClient;

    // -------------------------------------------------------------------------
    // 1) STRICT TRANSLATION for multilingual poster titles
    // -------------------------------------------------------------------------
    public String translateToLanguage(String text, String targetLang) {

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)   // correct latest lightweight model
                .addUserMessage("""
                        Translate this into %s using correct %s Unicode script only.
                        Do NOT transliterate, and do NOT mix English characters.
                        Output ONLY the translated text, nothing else:
                        %s
                        """.formatted(targetLang, targetLang, text))
                .build();

        ChatCompletion response = openAIClient.chat().completions().create(params);

        return response.choices().get(0).message().content()
                .orElse(text);
    }

    // -------------------------------------------------------------------------
    // 2) BASE POSTER IMAGE (AI-generated background)
    // -------------------------------------------------------------------------
    public byte[] generateBaseImage(String prompt) {

        ImageGenerateParams params = ImageGenerateParams.builder()
                .model(ImageModel.GPT_IMAGE_1)
                .prompt(prompt)
                .size(ImageGenerateParams.Size._1024X1024)
                .n(1L)
                .build();

        ImagesResponse response = openAIClient.images().generate(params);

        String b64 = response.data().get().get(0).b64Json()
                .orElseThrow(() -> new IllegalStateException("No image data returned by OpenAI"));

        return Base64.getDecoder().decode(b64);
    }

    // -------------------------------------------------------------------------
    // 3) MULTIMODAL FINAL POSTER GENERATION (Prompt + Base64 Images)
    // -------------------------------------------------------------------------
    public byte[] generateFinalPoster(String prompt,
                                      Path artistPath,
                                      Path channelPath,
                                      Path labelPath) throws IOException {

        StringBuilder multimodalPrompt = new StringBuilder(prompt);

        // Append uploaded asset images as Base64 strings
        if (artistPath != null) {
            multimodalPrompt.append("\nArtist Image (base64): ")
                    .append(encodeBase64(artistPath)).append("\n");
        }
        if (channelPath != null) {
            multimodalPrompt.append("\nChannel Logo (base64): ")
                    .append(encodeBase64(channelPath)).append("\n");
        }
        if (labelPath != null) {
            multimodalPrompt.append("\nProduction Label Logo (base64): ")
                    .append(encodeBase64(labelPath)).append("\n");
        }

        // Now request the final AI poster
        ImageGenerateParams params = ImageGenerateParams.builder()
                .model(ImageModel.GPT_IMAGE_1)
                .prompt(multimodalPrompt.toString())
                .size(ImageGenerateParams.Size._1024X1024)
                .n(1L)
                .build();

        ImagesResponse response = openAIClient.images().generate(params);

        String b64 = response.data().get().get(0).b64Json()
                .orElseThrow(() -> new IllegalStateException("Missing final poster image"));

        return Base64.getDecoder().decode(b64);
    }

    private String encodeBase64(Path path) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(path));
    }


    // -------------------------------------------------------------------------
    // 4) OPTIONAL LOCAL COMPOSITION ENGINE (Overlay logos + text)
    // -------------------------------------------------------------------------
    public byte[] composePoster(byte[] baseImageBytes,
                                Path artistImage,
                                Path channelLogo,
                                Path productionLogo,
                                String songTitle,
                                String artistName,
                                String channelName,
                                String productionLabel) throws IOException {

        BufferedImage base = ImageIO.read(new ByteArrayInputStream(baseImageBytes));
        int w = base.getWidth();
        int h = base.getHeight();

        Graphics2D g = base.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Soft black overlay for text region
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, h - (h / 3), w, h / 3);

            int padding = 40;

            // Artist image bottom-left
            if (artistImage != null && Files.exists(artistImage)) {
                BufferedImage artistImg = ImageIO.read(artistImage.toFile());
                BufferedImage scaled = scaleToHeight(artistImg, h / 3);
                g.drawImage(scaled, padding, h - scaled.getHeight() - padding, null);
                padding += scaled.getWidth() + 20;
            }

            // Channel logo top-right
            if (channelLogo != null && Files.exists(channelLogo)) {
                BufferedImage logo = ImageIO.read(channelLogo.toFile());
                BufferedImage scaled = scaleToHeight(logo, h / 6);
                g.drawImage(scaled, w - scaled.getWidth() - 30, 30, null);
            }

            // Production label bottom-right
            if (productionLogo != null && Files.exists(productionLogo)) {
                BufferedImage label = ImageIO.read(productionLogo.toFile());
                BufferedImage scaled = scaleToHeight(label, h / 8);
                g.drawImage(scaled, w - scaled.getWidth() - 30, h - scaled.getHeight() - 30, null);
            }

            // Title centered
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 52));
            drawCenteredString(g, songTitle, new Rectangle(0, h - (h / 3), w, h / 6), g.getFont());

            // Metadata
            g.setFont(new Font("SansSerif", Font.PLAIN, 26));
            int y = h - (h / 3) + (h / 6) + 20;
            if (artistName != null && !artistName.isBlank()) {
                g.drawString("Artist: " + artistName, 40, y); y += 34;
            }
            if (channelName != null && !channelName.isBlank()) {
                g.drawString("Channel: " + channelName, 40, y); y += 34;
            }
            if (productionLabel != null && !productionLabel.isBlank()) {
                g.drawString("Label: " + productionLabel, 40, y);
            }

        } finally {
            g.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(base, "jpg", out);
        return out.toByteArray();
    }

    private BufferedImage scaleToHeight(BufferedImage src, int targetHeight) {
        double ratio = targetHeight / (double) src.getHeight();
        int newW = (int) (src.getWidth() * ratio);

        BufferedImage dst = new BufferedImage(newW, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, newW, targetHeight, null);
        g.dispose();

        return dst;
    }

    private void drawCenteredString(Graphics2D g, String text, Rectangle rect, Font font) {
        if (text == null) return;
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.drawString(text, x, y);
    }
}
