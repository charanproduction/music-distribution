package com.digital.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.digital.entity.AppSetting;
import com.digital.repository.AppSettingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppSettingsService {

    private final AppSettingRepository settingRepo;

    public static final String KEY_POSTER_FEATURE_ENABLED = "AI_POSTER_ENABLED";

    /**
     * Global toggle: is AI poster feature enabled at all?
     */
    @Transactional(readOnly = true)
    public boolean isPosterFeatureEnabled() {
        Optional<AppSetting> opt = settingRepo.findByKey(KEY_POSTER_FEATURE_ENABLED);
        if (opt.isEmpty()) {
            // default ON if not explicitly configured
            return true;
        }
        return Boolean.parseBoolean(opt.get().getValue());
    }

    /**
     * Admin-only: enable/disable feature globally.
     */
    @Transactional
    public void setPosterFeatureEnabled(boolean enabled) {
        AppSetting setting = settingRepo.findByKey(KEY_POSTER_FEATURE_ENABLED)
                .orElseGet(() -> AppSetting.builder()
                        .key(KEY_POSTER_FEATURE_ENABLED)
                        .build());
        setting.setValue(Boolean.toString(enabled));
        settingRepo.save(setting);
    }
}
