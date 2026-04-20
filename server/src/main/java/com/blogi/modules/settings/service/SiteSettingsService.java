package com.blogi.modules.settings.service;

import com.blogi.modules.settings.dto.BlogSettingsRequest;
import com.blogi.modules.settings.dto.BlogSettingsResponse;
import com.blogi.modules.settings.entity.SiteSetting;
import com.blogi.modules.settings.mapper.SiteSettingMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteSettingsService {

    private static final String FOOTER_HTML_KEY = "footer_html";

    private final SiteSettingMapper siteSettingMapper;

    public SiteSettingsService(SiteSettingMapper siteSettingMapper) {
        this.siteSettingMapper = siteSettingMapper;
    }

    public BlogSettingsResponse getSettings() {
        return new BlogSettingsResponse(getValue(FOOTER_HTML_KEY));
    }

    @Transactional
    public BlogSettingsResponse updateSettings(BlogSettingsRequest request) {
        var footerHtml = request.footerHtml() == null ? "" : request.footerHtml();
        upsert(FOOTER_HTML_KEY, footerHtml);
        return new BlogSettingsResponse(footerHtml);
    }

    private String getValue(String key) {
        var setting = siteSettingMapper.selectById(key);
        if (setting == null || setting.getSettingValue() == null) {
            return "";
        }

        return setting.getSettingValue();
    }

    private void upsert(String key, String value) {
        var now = LocalDateTime.now();
        var setting = siteSettingMapper.selectById(key);
        if (setting == null) {
            setting = new SiteSetting();
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            setting.setUpdatedAt(now);
            siteSettingMapper.insert(setting);
            return;
        }

        setting.setSettingValue(value);
        setting.setUpdatedAt(now);
        siteSettingMapper.updateById(setting);
    }
}
