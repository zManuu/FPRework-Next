package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.events.OnEnable;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.timer.Timer;
import de.fantasypixel.rework.framework.timer.TimerManager;
import de.fantasypixel.rework.framework.web.WebGet;
import de.fantasypixel.rework.framework.web.WebPost;
import de.fantasypixel.rework.framework.web.WebResponse;
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

    @Timer(interval = 500, type = TimerManager.TimerType.ASYNC)
    public void testTimer() {
        this.accountService.logTimerRuns(++this.timerRuns);
    }

    @WebGet(name = "get-index", route = "/")
    public WebResponse getHelloWorld() {
        return new WebResponse(201, "Hello World!");
    }

    record ServerStatus(String motd, int playerCount) {}

    @WebGet(name = "get-server-status", route = "/api/v1/server-status")
    public WebResponse getServerStatus() {
        return new WebResponse(
                201,
                new ServerStatus("Hello, I'm the MOTD!", 100)
        );
    }

    public record Player(String id, String name, int level) {}

    @WebGet(name = "get-player", route = "/api/v1/player/get")
    public WebResponse getPlayer(String id) {
        return new WebResponse(
                201,
                new Player(id, "zManuu", 100)
        );
    }

    @WebPost(name = "post-player", route = "/api/v1/player/post")
    public WebResponse postPlayer(String _1, Player player) {
        return new WebResponse(
                201,
                new Player(player.id(), player.name(), 0)
        );
    }

}
