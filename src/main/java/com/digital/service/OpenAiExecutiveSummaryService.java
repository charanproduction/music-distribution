package com.digital.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenAiExecutiveSummaryService {

    @Value("${openai.api.key:}")
    private String apiKey;

    private final RestTemplateBuilder restTemplateBuilder;

    public String generateExecutiveSummary(String year, String label, Map<String, Object> stats) {

        if (apiKey == null || apiKey.isBlank()) {
            return fallbackSummary(stats);
        }

        try {
            RestTemplate rt = restTemplateBuilder.build();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = """
                You are an executive music distribution analytics advisor.
                Generate a professional leadership summary using the following data.

                Filter Year: %s
                Filter Label: %s

                Data:
                %s

                Output format:
                1. Executive Summary
                2. Revenue Drivers
                3. Growth Opportunities
                4. Risk Areas
                5. Recommended Actions for Next Quarter
                """.formatted(year == null ? "ALL" : year, label == null ? "ALL" : label, stats);

            Map<String, Object> body = Map.of(
                    "model", "gpt-4.1-mini",
                    "input", prompt
            );

            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = rt.postForEntity(
                    "https://api.openai.com/v1/responses",
                    req,
                    Map.class
            );

            return extractText(response.getBody());

        } catch (Exception ex) {
            return fallbackSummary(stats);
        }
    }

    private String extractText(Map body) {
        if (body == null) return "AI summary unavailable.";

        Object outputText = body.get("output_text");
        if (outputText != null) return outputText.toString();

        return "AI summary generated, but response text could not be parsed. Please check OpenAI response format.";
    }

    private String fallbackSummary(Map<String, Object> stats) {
        return """
                AI summary unavailable, showing rule-based insight.

                Key observations:
                - Unique Songs: %s
                - Unique Artists: %s
                - Net Revenue: %s

                Recommended actions:
                1. Promote top-performing songs and labels.
                2. Review underperforming regions and platforms.
                3. Build next-quarter campaigns around high-revenue platforms.
                4. Improve metadata quality for low-performing catalog items.
                """.formatted(
                stats.get("uniqueSongs"),
                stats.get("uniqueArtists"),
                stats.get("netRevenue")
        );
    }
}
