package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.framework.discord.FPDiscordChannel;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.discord.DiscordService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Controller
public class AccountController implements Listener {

    @Service private AccountService accountService;
    @Service private NotificationService notificationService;
    @Service private DiscordService discordService;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString();
        Account account;

        event.setJoinMessage("§7[§a+§7] " + playerName);
        this.discordService.sendMessage(FPDiscordChannel.SERVER_CHAT, "[+] {0}", playerName);

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
        this.discordService.sendMessage(FPDiscordChannel.SERVER_CHAT, "[-] {0}", playerName);
    }

}
