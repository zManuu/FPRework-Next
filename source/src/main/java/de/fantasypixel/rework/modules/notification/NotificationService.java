package de.fantasypixel.rework.modules.notification;

import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.language.LanguageService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * A service that manages all notifications send to players like chat-messages, boos-titles or action-bar.
 * Note: This service is the only one that communicates with a {@link org.bukkit.entity.Player} object.
 * <br><br>
 * <b>Color-Codes:</b>
 * <table>
 *   <tr><td>&0</td><td>black</td></tr>
 *   <tr><td>&1</td><td>dark blue</td></tr>
 *   <tr><td>&2</td><td>dark green</td></tr>
 *   <tr><td>&3</td><td>dark aqua</td></tr>
 *   <tr><td>&4</td><td>dark red</td></tr>
 *   <tr><td>&5</td><td>dark purple</td></tr>
 *   <tr><td>&6</td><td>gold</td></tr>
 *   <tr><td>&7</td><td>gray</td></tr>
 *   <tr><td>&8</td><td>dark gray</td></tr>
 *   <tr><td>&9</td><td>blue</td></tr>
 *   <tr><td>&a</td><td>green</td></tr>
 *   <tr><td>&b</td><td>aqua</td></tr>
 *   <tr><td>&c</td><td>red</td></tr>
 *   <tr><td>&d</td><td>light purple</td></tr>
 *   <tr><td>&e</td><td>yellow</td></tr>
 *   <tr><td>&f</td><td>white</td></tr>
 * </table>
 * <br>
 * <b>Format-Codes:</b>
 * <table>
 *   <tr><td>&k</td><td>random</td></tr>
 *   <tr><td>&l</td><td>bold</td></tr>
 *   <tr><td>&m</td><td>strikethrough</td></tr>
 *   <tr><td>&n</td><td>underline</td></tr>
 *   <tr><td>&o</td><td>italic</td></tr>
 *   <tr><td>&r</td><td>reset</td></tr>
 * </table>
 */
@ServiceProvider
public class NotificationService {

    @Config private NotificationConfig notificationConfig;
    @Service private LanguageService languageService;
    @Service private AccountService accountService;

    private void sendChatMessage(@Nonnull Player player, @Nonnull String message) {
        player.sendMessage(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    String.format(
                            "%s %s",
                            this.notificationConfig.getChatPluginPrefix(),
                            message
                    )
                )
        );
    }

    public void sendChatMessage(@Nonnull Player player, @Nonnull String languageKey, @Nullable Object... args) {
        var accountId = this.accountService.getAccount(player.getUniqueId()).getId();
        this.sendChatMessage(player, this.languageService.getTranslation(accountId, languageKey, args));
    }

    public void sendChatMessage(@Nonnull Player player, @Nonnull String languageKey, @Nonnull Map<String, Object> args) {
        var accountId = this.accountService.getAccount(player.getUniqueId()).getId();
        this.sendChatMessage(player, this.languageService.getTranslation(accountId, languageKey, args));
    }

}
