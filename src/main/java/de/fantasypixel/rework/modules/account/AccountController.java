package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.utils.command.Command;
import de.fantasypixel.rework.utils.modules.Controller;
import org.bukkit.command.CommandSender;

public class AccountController extends Controller<AccountService> {

    public AccountController(AccountService service) {
        super(service);
    }

    @Override
    public void onEnable() {
        System.out.println("Hi from account-controller!");
    }

    @Command(name = "ping")
    public void testCmd(CommandSender sender, String[] args) {
        sender.sendMessage("pong");
    }

}
