package com.digital.service;

//import com.openai.OpenAI;
//import com.openai.api.images.Images;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPosterGeneratorService {

//    private final OpenAI openAI; // Inject using config
//
//    public byte[] generatePosterImage(String prompt) {
//        var response = openAI.images()
//                .generate(Images.GenerateRequest.builder()
//                        .model("gpt-image-1")
//                        .prompt(prompt)
//                        .size("1024x1024")
//                        .build()
//                );
//
//        return response.data().get(0).b64Json(); // image bytes
//    }
	public byte[] generatePosterImage(String title, String language) throws Exception {

        int width = 1024, height = 1024;
        BufferedImage poster = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = poster.createGraphics();

        // Background Gradient 🎨
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(30, 30, 80),
                width, height, new Color(255, 0, 90)
        );
        g.setPaint(gp);
        g.fillRect(0, 0, width, height);

        // Title Text ✨
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 80));

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(title);
        int x = (width - textWidth) / 2;
        int y = height / 2;
        g.drawString(title, x, y);

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(poster, "png", baos);

        return baos.toByteArray();
    }
}
