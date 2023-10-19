package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.utils.events.OnEnable;
import de.fantasypixel.rework.utils.provider.Controller;
import de.fantasypixel.rework.utils.provider.Service;
import de.fantasypixel.rework.utils.spigotevents.SpigotEvent;
import org.bukkit.event.player.PlayerJoinEvent;

@Controller
public class AccountController {

    @Service(name = "account")
    private AccountService accountService;

    @OnEnable
    public void onEnable() {
        this.accountService.sayHello();
        this.accountService.testAccountRepo();
    }

    @SpigotEvent
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.accountService.logPlayerJoin(event.getPlayer().getName());
    }

}
