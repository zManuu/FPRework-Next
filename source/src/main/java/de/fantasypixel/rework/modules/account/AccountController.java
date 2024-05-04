package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.modules.events.AccountLoginEvent;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.language.LanguageService;
import de.fantasypixel.rework.modules.menu.MenuService;
import de.fantasypixel.rework.modules.utils.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Controller
public class AccountController implements Listener {

    @Service private AccountService accountService;
    @Service private DateUtils dateUtils;
    @Service private LanguageService languageService;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString();
        Account account;

        event.setJoinMessage("[+] " + playerName);

        if (!this.accountService.hasAccount(playerUuid)) {
            // first join -> setup account
            account = this.accountService.createAccount(playerUuid, playerName, null);
            player.sendMessage(this.languageService.getTranslation(null, "welcome", playerName, account.getId()));
        } else {
            account = this.accountService.getAccount(playerUuid);
        }

        // login
        // todo: move this logic to service
        account.setLastLogin(this.dateUtils.getCurrentDateTime());
        Bukkit.getPluginManager().callEvent(new AccountLoginEvent(account, player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage("[-] " + event.getPlayer().getName());
    }

}
