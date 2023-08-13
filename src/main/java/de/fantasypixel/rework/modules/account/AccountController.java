package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.utils.events.OnEnable;
import de.fantasypixel.rework.utils.provider.Controller;
import de.fantasypixel.rework.utils.provider.Service;
import de.fantasypixel.rework.utils.spigotevents.SpigotEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;

@Controller
public class AccountController {

    @Service(name = "account")
    private AccountService accountService;

    @OnEnable
    public void onEnable() {
        this.accountService.sayHello();
    }

    @SpigotEvent
    public void onPlayerJoin(PlayerJoinEvent event) {
        System.out.println("bac");
        event.setJoinMessage("Hey");
    }

}
