package de.fantasypixel.rework.modules.account.options;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import org.bukkit.entity.Player;

@Controller
public class AccountOptionsController {

    @Service private AccountOptionsService accountOptionsService;
    @Service private AccountService accountService;
    @Service private NotificationService notificationService;

    @Command(name = "options", aliases = {"settings", "account"})
    public void optionsCommand(Player player, String[] args) {
        var account = this.accountService.getAccount(player.getUniqueId());
        var accountId = account.getId();

        if (args.length == 0) {

            var optionsText = this.accountOptionsService.getOptionsText(accountId);
            this.notificationService.sendChatMessage(player, "options-list", (Object[]) optionsText);

        } else if (args.length == 3 && args[0].equalsIgnoreCase("update")) {
            var optionKey = args[1];
            var optionValue = args[2];

            try {
                var updateSuccess = this.accountOptionsService.updateOptions(accountId, optionKey, optionValue);

                if (!updateSuccess) {
                    this.notificationService.sendChatMessage(player, "options-update-error");
                    throw new IllegalArgumentException("Options couldn't be updated properly.");
                } else
                    this.notificationService.sendChatMessage(player, "options-update-success");

            } catch (IllegalArgumentException ex) {
                if (ex.getMessage() == null) {
                    // invalid option
                    this.notificationService.sendChatMessage(player, "options-invalid-option", this.accountOptionsService.getOptionDefinitionsText());
                } else {
                    // invalid value
                    this.notificationService.sendChatMessage(player, "options-invalid-value", ex.getMessage());
                }
            }

        } else {
            this.notificationService.sendChatMessage(player, "options-invalid-syntax");
        }
    }

}
