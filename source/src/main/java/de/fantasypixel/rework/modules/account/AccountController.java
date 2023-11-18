package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.events.OnEnable;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.timer.Timer;
import de.fantasypixel.rework.framework.timer.TimerType;
import de.fantasypixel.rework.framework.web.WebGet;
import de.fantasypixel.rework.framework.web.WebResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@Controller
public class AccountController implements Listener {

    @Service(name = "account")
    private AccountService accountService;

    private int timerRuns = 0;

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

    @Command(name = "test")
    public void onTestCommand(Player player, String[] args) {
        this.accountService.logPlayerTestCommand(args);
    }

    @Timer(interval = 500, type = TimerType.ASYNC)
    public void testTimer() {
        this.accountService.logTimerRuns(++this.timerRuns);
    }

    @WebGet(route = "/")
    public WebResponse getHelloWorld() {
        return new WebResponse(201, "Hello World!");
    }

}
