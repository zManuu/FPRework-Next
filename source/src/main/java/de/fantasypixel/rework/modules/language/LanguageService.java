package de.fantasypixel.rework.modules.language;

import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.jsondata.JsonData;
import de.fantasypixel.rework.framework.jsondata.JsonDataContainer;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.account.options.AccountOptionsService;
import de.fantasypixel.rework.modules.utils.FormatUtils;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Dictionary;
import java.util.Map;
import java.util.UUID;

@ServiceProvider
public class LanguageService {

    @Service private AccountOptionsService accountOptionsService;
    @JsonData private JsonDataContainer<Language> languageContainer;
    @Auto private FPLogger logger;
    @Config private LanguageConfig languageConfig;
    @Service private FormatUtils formatUtils;
    @Service private AccountService accountService;

    /**
     * Gets the dictionary of a language
     * @param languageKey the key of the language (de, en)
     * @return the dictionary of a language
     * @throws IllegalArgumentException if no dictionary was found with a matching language key
     */
    @Nonnull
    public Map<String, String> getDictionary(@Nullable String languageKey) throws IllegalArgumentException {
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
            this.logger.warning("Language entry-key {0} wasn't found for language {1}", entryKey, languageKey);
            return dictionary.get("404");
        } else
            return this.formatUtils.format(translation, args);
    }

    /**
     * @see de.fantasypixel.rework.modules.account.AccountService#getAccount(UUID)
     * @see LanguageService#getTranslation(Integer, String, Map)
     */
    @Nonnull
    public String getTranslation(@Nonnull Player player, @Nonnull String entryKey, @Nullable Map<String, Object> args) {
        var account = this.accountService.getAccount(player.getUniqueId());
        return this.getTranslation(account.getId(), entryKey, args);
    }

    @Nonnull
    public String getTranslation(@Nullable Integer accountId, @Nonnull String entryKey, @Nullable Object... args) {
        var languageKey = accountId != null
                ? this.accountOptionsService.getOptions(accountId).getLanguageKey()
                : this.languageConfig.getDefaultLanguageKey();

        var dictionary = this.getDictionary(languageKey);

        var translation = dictionary.get(entryKey);
        if (translation == null) {
            this.logger.warning("Language entry-key {0} wasn't found for language {1}", entryKey, languageKey);
            return dictionary.get("404");
        } else
            return this.formatUtils.format(translation, args);
    }

    /**
     * @see de.fantasypixel.rework.modules.account.AccountService#getAccount(UUID)
     * @see LanguageService#getTranslation(Integer, String, Object...)
     */
    @Nonnull
    public String getTranslation(@Nonnull Player player, @Nonnull String entryKey, @Nullable Object... args) {
        var account = this.accountService.getAccount(player.getUniqueId());
        return this.getTranslation(account.getId(), entryKey, args);
    }

    /**
     * Gets an optional translation.
     * @param accountId the account's id
     * @param entryKey the key of the dictionary entry
     * @param args possible arguments
     * @return the translation or null if no translation was found for the language the account id using
     */
    @Nullable
    public String getTranslationOptional(@Nullable Integer accountId, @Nonnull String entryKey, @Nullable Map<String, Object> args) {
        var languageKey = accountId != null
                ? this.accountOptionsService.getOptions(accountId).getLanguageKey()
                : this.languageConfig.getDefaultLanguageKey();

        var dictionary = this.getDictionary(languageKey);
        var translation = dictionary.get(entryKey);

        return translation == null
                ? null
                : this.formatUtils.format(translation, args);
    }

}
