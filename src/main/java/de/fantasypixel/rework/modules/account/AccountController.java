package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.utils.events.OnEnable;
import de.fantasypixel.rework.utils.provider.Controller;
import de.fantasypixel.rework.utils.provider.Service;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;

@Controller
public class AccountController implements Listener {

    @Service(name = "account")
    private AccountService accountService;

    @OnEnable
    public void onEnable() {
        this.accountService.sayHello();
        this.accountService.testAccountRepo();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev) {
        this.accountService.logPlayerJoin(ev.getPlayer().getDisplayName());
        ev.setJoinMessage("HII");
    }

}
