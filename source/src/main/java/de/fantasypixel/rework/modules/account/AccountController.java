package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.framework.discord.FPDiscordChannel;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.discord.DiscordService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.utils.DateUtils;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.MessageFormat;

@Controller
public class AccountController implements Listener {

    @Service private AccountService accountService;
    @Service private NotificationService notificationService;
    @Service private DiscordService discordService;
    @Service private DateUtils dateUtils;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString();
        Account account;

        event.setJoinMessage("§7[§a+§7] " + playerName);
        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_USER,
                EmbedCreateSpec.builder()
                        .color(Color.GREEN)
                        .title("Server status")
                        .description(MessageFormat.format("[+] {0} ({1})", playerName, this.dateUtils.getCurrentDateTime()))
                        .build()
        );

        if (!this.accountService.hasAccount(playerUuid)) {
            // first join -> setup account
            account = this.accountService.createAccount(playerUuid, playerName, null);
            this.notificationService.sendChatMessage(player, "welcome", playerName, account.getId());
        } else {
            account = this.accountService.getAccount(playerUuid);
        }

        this.accountService.login(player, account);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var playerName = event.getPlayer().getName();

        event.setQuitMessage("§7[§c-§7] " + playerName);
        this.discordService.sendEmbed(
                FPDiscordChannel.LOGS_USER,
                EmbedCreateSpec.builder()
                        .color(Color.RED)
                        .title("Server status")
                        .description(MessageFormat.format("[-] {0} ({1})", playerName, this.dateUtils.getCurrentDateTime()))
                        .build()
        );
    }

}
