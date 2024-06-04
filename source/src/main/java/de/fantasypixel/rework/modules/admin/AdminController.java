package de.fantasypixel.rework.modules.admin;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.notification.NotificationType;
import de.fantasypixel.rework.modules.sound.Sound;
import de.fantasypixel.rework.modules.sound.SoundService;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

@Controller
public class AdminController {

    @Service private AdminService adminService;
    @Service private NotificationService notificationService;
    @Service private SoundService soundService;

    @Command(name = "admin")
    public void onAdminCommand(Player player, String[] args) {

        if (args.length == 1 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl"))) {

            // reload
            this.adminService.doReload(player);

        } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("sound")) {
            
            // play sound (simple, not using the SoundService, just to test sounds)
            org.bukkit.Sound sound;
            SoundCategory soundCategory;

            try {
                sound = org.bukkit.Sound.valueOf(args[1]);
                soundCategory = args.length == 3
                        ? SoundCategory.valueOf(args[2])
                        : SoundCategory.NEUTRAL;
            } catch (IllegalArgumentException ex) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "admin-sound-invalid");
                this.soundService.playSound(player, Sound.WARNING);
                return;
            }

            this.notificationService.sendChatMessage(NotificationType.UNKNOWN, player, "admin-sound");
            player.playSound(player, sound, soundCategory, 1, 1);

        }


    }

}
