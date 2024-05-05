package de.fantasypixel.rework.modules.language;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.jsondata.JsonData;
import de.fantasypixel.rework.framework.jsondata.JsonDataContainer;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import de.fantasypixel.rework.modules.account.options.AccountOptionsService;
import de.fantasypixel.rework.modules.utils.FormatUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.Map;

@ServiceProvider
public class LanguageService {

    @Service private AccountOptionsService accountOptionsService;
    @JsonData private JsonDataContainer<Language> languageContainer;
    @Plugin private FPRework plugin;
    @Config private LanguageConfig languageConfig;
    @Service private FormatUtils formatUtils;

    @Nonnull
    public Map<String, String> getDictionary(@Nullable String languageKey) {
        return this.languageContainer.getEntries()
                .stream()
                .filter(language -> language.getKey().equalsIgnoreCase(languageKey))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .getDictionary();
    }

    @Nonnull
    public String getTranslation(@Nullable Integer accountId, @Nonnull String entryKey, @Nullable Map<String, Object> args) {
        var languageKey = accountId != null
            ? this.accountOptionsService.getOptions(accountId).getLanguageKey()
            : this.languageConfig.getDefaultLanguageKey();

        var dictionary = this.getDictionary(languageKey);

        var translation = dictionary.get(entryKey);
        if (translation == null) {
            this.plugin.getFpLogger().warning("Language entry-key {0} wasn't found for language {1}", entryKey, languageKey);
            return dictionary.get("404");
        } else
            return this.formatUtils.format(translation, args);
    }

    @Nonnull
    public String getTranslation(@Nullable Integer accountId, @Nonnull String entryKey, @Nullable Object... args) {
        var languageKey = accountId != null
                ? this.accountOptionsService.getOptions(accountId).getLanguageKey()
                : this.languageConfig.getDefaultLanguageKey();

        var dictionary = this.getDictionary(languageKey);

        var translation = dictionary.get(entryKey);
        if (translation == null) {
            this.plugin.getFpLogger().warning("Language entry-key {0} wasn't found for language {1}", entryKey, languageKey);
            return dictionary.get("404");
        } else
            return this.formatUtils.format(translation, args);
    }

}
