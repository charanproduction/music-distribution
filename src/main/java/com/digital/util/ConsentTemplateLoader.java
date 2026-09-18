package com.digital.util;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.digital.entity.User;

@Component
public class ConsentTemplateLoader {

    public String loadTemplate(User user, String fullName) throws Exception {
        ClassPathResource resource =
                new ClassPathResource("templates/consent/rights-consent-template.html");

        String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        return html
                .replace("{{CHANNEL_NAME}}", user.getChannelName())
                .replace("{{FULL_NAME}}", fullName)
                .replace("{{EMAIL}}", user.getEmail());
    }
}
