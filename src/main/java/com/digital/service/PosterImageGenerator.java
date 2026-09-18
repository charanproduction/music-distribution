package com.digital.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PosterImageGenerator {

    private PosterImageGenerator() {}

    public static BufferedImage generateSimplePoster(String title,
                                                     String language,
                                                     String mood) throws Exception {

        int width = 2000;
        int height = 2000;

        String safeTitle = title == null || title.isBlank() ? "Untitled" : title.trim();
        String safeLanguage = language == null ? "" : language.trim();
        String safeMood = mood == null ? "" : mood.trim();

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            Color top;
            Color bottom;

            switch (safeMood.toLowerCase()) {
                case "dark":
                    top = new Color(12, 18, 35);
                    bottom = new Color(45, 65, 115);
                    break;
                case "romantic":
                    top = new Color(140, 26, 67);
                    bottom = new Color(250, 100, 130);
                    break;
                case "party":
                    top = new Color(30, 85, 210);
                    bottom = new Color(20, 185, 135);
                    break;
                case "calm":
                    top = new Color(24, 90, 157);
                    bottom = new Color(67, 206, 162);
                    break;
                default:
                    top = new Color(70, 55, 190);
                    bottom = new Color(35, 180, 225);
            }

            GradientPaint paint = new GradientPaint(0, 0, top, width, height, bottom);
            g.setPaint(paint);
            g.fillRect(0, 0, width, height);

            int outerMargin = 110;

            g.setColor(new Color(255, 255, 255, 45));
            g.setStroke(new BasicStroke(8));
            g.drawRoundRect(
                    outerMargin,
                    outerMargin,
                    width - outerMargin * 2,
                    height - outerMargin * 2,
                    80,
                    80
            );

            Shape card = new RoundRectangle2D.Double(
                    180,
                    220,
                    width - 360,
                    height - 440,
                    90,
                    90
            );
            g.setColor(new Color(0, 0, 0, 105));
            g.fill(card);

            drawDecorativeCircle(g, 300, 320, 260, new Color(255, 255, 255, 34));
            drawDecorativeCircle(g, width - 470, height - 520, 360, new Color(255, 255, 255, 28));

            Rectangle titleArea = new Rectangle(260, 430, width - 520, 520);
            drawWrappedCenteredTitle(g, safeTitle, safeLanguage, titleArea);

            if (!safeLanguage.isBlank()) {
                g.setFont(resolveFont(safeLanguage, Font.PLAIN, 64));
                g.setColor(new Color(235, 240, 255));
                drawCenteredString(g, safeLanguage + " Music Release",
                        new Rectangle(0, 1010, width, 100));
            }

            if (!safeMood.isBlank()) {
                g.setFont(resolveFont(safeLanguage, Font.BOLD, 58));
                g.setColor(new Color(255, 255, 255, 220));
                drawCenteredString(g, "#" + safeMood.toUpperCase(),
                        new Rectangle(0, 1160, width, 90));
            }

            g.setFont(resolveFont(safeLanguage, Font.BOLD, 72));
            g.setColor(new Color(255, 255, 255, 230));
            drawCenteredString(g, "MUSIC SINGLE",
                    new Rectangle(0, 1500, width, 100));

            g.setFont(resolveFont(safeLanguage, Font.PLAIN, 48));
            g.setColor(new Color(220, 225, 235));
            drawCenteredString(g, "Charan Production Digital",
                    new Rectangle(0, 1690, width, 80));

        } finally {
            g.dispose();
        }

        return img;
    }

    private static void drawWrappedCenteredTitle(Graphics2D g,
                                                 String text,
                                                 String language,
                                                 Rectangle area) {

        int maxFontSize = 165;
        int minFontSize = 58;

        for (int size = maxFontSize; size >= minFontSize; size -= 4) {
            Font font = resolveFont(language, Font.BOLD, size);
            g.setFont(font);

            List<String> lines = wrapText(g, text, area.width);
            int lineHeight = g.getFontMetrics().getHeight();
            int totalHeight = lineHeight * lines.size();

            if (lines.size() <= 4 && totalHeight <= area.height) {
                drawTitleLines(g, lines, area, lineHeight);
                return;
            }
        }

        g.setFont(resolveFont(language, Font.BOLD, minFontSize));
        List<String> lines = wrapText(g, text, area.width);
        if (lines.size() > 4) {
            lines = lines.subList(0, 4);
            int lastIndex = lines.size() - 1;
            lines.set(lastIndex, trimToFit(g, lines.get(lastIndex) + "...", area.width));
        }

        drawTitleLines(g, lines, area, g.getFontMetrics().getHeight());
    }

    private static void drawTitleLines(Graphics2D g,
                                       List<String> lines,
                                       Rectangle area,
                                       int lineHeight) {

        FontMetrics fm = g.getFontMetrics();
        int totalHeight = lineHeight * lines.size();
        int y = area.y + (area.height - totalHeight) / 2 + fm.getAscent();

        for (String line : lines) {
            int x = area.x + (area.width - fm.stringWidth(line)) / 2;

            // shadow
            g.setColor(new Color(0, 0, 0, 150));
            g.drawString(line, x + 5, y + 5);

            // title
            g.setColor(new Color(255, 246, 220));
            g.drawString(line, x, y);

            y += lineHeight;
        }
    }

    private static List<String> wrapText(Graphics2D g, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.isBlank()) {
            lines.add("Untitled");
            return lines;
        }

        String[] words = text.trim().split("\\s+");

        // For languages/scripts without spaces, split by character if the full title is too wide.
        if (words.length == 1 && g.getFontMetrics().stringWidth(text) > maxWidth) {
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                String test = current.toString() + text.charAt(i);
                if (g.getFontMetrics().stringWidth(test) <= maxWidth) {
                    current.append(text.charAt(i));
                } else {
                    if (current.length() > 0) {
                        lines.add(current.toString());
                    }
                    current = new StringBuilder(String.valueOf(text.charAt(i)));
                }
            }
            if (current.length() > 0) {
                lines.add(current.toString());
            }
            return lines;
        }

        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String test = current.length() == 0 ? word : current + " " + word;

            if (g.getFontMetrics().stringWidth(test) <= maxWidth) {
                current = new StringBuilder(test);
            } else {
                if (current.length() > 0) {
                    lines.add(current.toString());
                }

                if (g.getFontMetrics().stringWidth(word) > maxWidth) {
                    lines.add(trimToFit(g, word, maxWidth));
                    current = new StringBuilder();
                } else {
                    current = new StringBuilder(word);
                }
            }
        }

        if (current.length() > 0) {
            lines.add(current.toString());
        }

        return lines;
    }

    private static String trimToFit(Graphics2D g, String text, int maxWidth) {
        if (text == null) return "";
        String result = text;

        while (result.length() > 1 && g.getFontMetrics().stringWidth(result) > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    private static void drawCenteredString(Graphics2D g, String text, Rectangle rect) {
        if (text == null || text.isBlank()) return;

        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();

        g.drawString(text, Math.max(x, rect.x + 20), y);
    }

    private static void drawDecorativeCircle(Graphics2D g, int x, int y, int size, Color color) {
        g.setColor(color);
        g.fillOval(x, y, size, size);
    }

    private static Font resolveFont(String language, int style, int size) {
        String fontName = "SansSerif";

        if ("Hindi".equalsIgnoreCase(language)
                || "Odia".equalsIgnoreCase(language)
                || "Oriya".equalsIgnoreCase(language)) {
            fontName = "Nirmala UI";
        }

        return new Font(fontName, style, size);
    }
}