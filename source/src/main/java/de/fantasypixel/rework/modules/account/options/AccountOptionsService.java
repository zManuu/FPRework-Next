package de.fantasypixel.rework.modules.account.options;

import com.google.gson.Gson;
import de.fantasypixel.rework.framework.discord.FPDiscordChannel;
import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.database.Query;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.discord.DiscordService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.notification.NotificationType;
import de.fantasypixel.rework.modules.sound.Sound;
import de.fantasypixel.rework.modules.sound.SoundService;
import discord4j.rest.util.Color;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Locale;

@ServiceProvider
public class AccountOptionsService {

    private final static String CLASS_NAME = AccountOptionsService.class.getSimpleName();

    @Auto private FPLogger logger;
    @Auto private Gson gson;
    @DataRepo private DataRepoProvider<AccountOptions> dataRepo;
    @Config private AccountOptionsConfig config;
    @Service private NotificationService notificationService;
    @Service private SoundService soundService;
    @Service private DiscordService discordService;

    /**
     * Creates a new account options entry in the database that uses the default options.
     * @param accountId the id of the associated account
     */
    private void createDefaultOptions(int accountId) {
        var defaultOptions = this.gson.fromJson(
                this.gson.toJson(this.config.getDefaultOptions()),
                AccountOptions.class
        );

        defaultOptions.setAccountId(accountId);

        if (this.dataRepo.insert(defaultOptions)) {
            this.logger.debug("Created default account-options for account {0}.", accountId);
        } else {
            this.logger.warning(CLASS_NAME, "createDefaultOptions", "Couldn't insert default account-options for account {0}.", accountId);
        }
    }

    /**
     * Retrieves the account options from the data-repository.
     * If none are found, the default options will be applied.
     * @param accountId the id of the associated account
     * @return the current account options
     */
    @Nonnull
    public AccountOptions getOptions(int accountId) {
        if (this.dataRepo.exists(new Query("accountId", accountId))) {
            var existingOptions = this.dataRepo.get(new Query("accountId", accountId));

            if (existingOptions == null) {
                this.logger.error(CLASS_NAME, "getOptions", "Tried to retrieve existing options for account {0}, but found none.", accountId);
                return this.config.getDefaultOptions();
            }

            return existingOptions;
        } else {
            this.createDefaultOptions(accountId);
            return this.config.getDefaultOptions();
        }
    }

    /**
     * Updates one option in the database.
     * The validation is done in this method.
     * @throws IllegalArgumentException if an option hasn't been found (-> no message) or if the given value was invalid (with message)
     * @return weather the update was successful
     */
    public boolean updateOptions(@Nonnull Player player, int accountId, @Nonnull String option, @Nonnull String value) throws IllegalArgumentException {
        var options = this.getOptions(accountId);

        if (option.equalsIgnoreCase("language") || option.equalsIgnoreCase("lang")) {

            // change language
            if (!value.equalsIgnoreCase("de") && !value.equalsIgnoreCase("en"))
                throw new IllegalArgumentException("de, en");

            options.setLanguageKey(value);
            this.notificationService.sendChatMessage(NotificationType.SUCCESS, player, "options-language-updated", value);

        } else if (option.equalsIgnoreCase("build")) {

            // change build-mode
            if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
                throw new IllegalArgumentException("true, false");

            var boolValue = value.equalsIgnoreCase("true");
            options.setBuildMode(boolValue);
            this.notificationService.sendChatMessage(
                    NotificationType.SUCCESS,
                    player,
                    boolValue ? "options-build-activated" : "options-build-deactivated",
                    value
            );
            this.soundService.playSound(player, Sound.BUILD_MODE);

        } else {
            // no matching option
            throw new IllegalArgumentException();
        }

        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_USER,
                Color.GREEN,
                "Account options update",
                "Player \"{0}\" (account {1}) changed the account-option \"{2}\" to \"{3}\"",
                player.getName(),
                accountId,
                option,
                value
        );

        return this.dataRepo.update(options);
    }

    /**
     * @param accountId the id of the associated account
     * @return a text-representation of the account's options
     */
    @Nonnull
    public String[] getOptionsText(int accountId) {
        var options = this.getOptions(accountId);
        return new String[] {
                options.getLanguageKey(),
                String.valueOf(options.isBuildMode())
        };
    }

    /**
     * @return a text-representation of all options and their possible values
     */
    @Nonnull
    public String getOptionDefinitionsText() {
        return String.join("\n", new String[] {
                "language - de / en",
                "build - true / false"
        });
    }

}
