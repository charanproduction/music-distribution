package com.digital.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.digital.service.AppSettingsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final AppSettingsService appSettingsService;

    @GetMapping("/posters")
    public String posterSettings(Model model) {
        model.addAttribute("posterEnabled", appSettingsService.isPosterFeatureEnabled());
        return "admin/poster-settings";
    }

    @PostMapping("/posters")
    public String updatePosterSettings(@RequestParam(name = "enabled", defaultValue = "false") boolean enabled) {
        appSettingsService.setPosterFeatureEnabled(enabled);
        return "redirect:/admin/settings/posters?updated";
    }
}
