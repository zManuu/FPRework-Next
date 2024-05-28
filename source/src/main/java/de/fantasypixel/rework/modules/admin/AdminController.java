package de.fantasypixel.rework.modules.admin;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import org.bukkit.entity.Player;

@Controller
public class AdminController {

    @Service private AdminService adminService;

    @Command(name = "admin")
    public void onAdminCommand(Player player, String[] args) {

        if (args.length == 1 && args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
            this.adminService.doReload(player);
        }

    }

}
