package de.fantasypixel.rework.modules.account.options;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.account.AccountService;
import org.bukkit.entity.Player;

@Controller
public class AccountOptionsController {

    @Service private AccountOptionsService accountOptionsService;
    @Service private AccountService accountService;

    @Command(name = "options", aliases = {"settings", "account"})
    public void optionsCommand(Player player, String[] args) {
        var account = this.accountService.getAccount(player.getUniqueId());

        if (args.length == 0) {

            player.sendMessage("Deine Account-Optionen:");
            player.sendMessage(this.accountOptionsService.getOptionsText(account.getId()));

        } else if (args.length == 3 && args[0].equalsIgnoreCase("update")) {
            var optionKey = args[1];
            var optionValue = args[2];

            try {
                var updateSuccess = this.accountOptionsService.updateOptions(account.getId(), optionKey, optionValue);

                if (!updateSuccess) {
                    player.sendMessage("Deine Änderung konnte nicht gespeichert werden! Bitte melde dich an den Support, damit wir dir helfen können.");
                    throw new IllegalArgumentException("Options couldn't be updated properly.");
                } else
                    player.sendMessage("Deine Änderung wurde gespeichert.");

            } catch (IllegalArgumentException ex) {
                if (ex.getMessage() == null) {
                    // invalid option
                    player.sendMessage("Die Option wurde nicht gefunden.");
                    player.sendMessage("Die folgenden Optionen sind verfügbar:");
                    player.sendMessage(this.accountOptionsService.getOptionDefinitionsText());
                } else {
                    // invalid value
                    player.sendMessage("Falscher Wert! Es wird folgendes erwartet: " + ex.getMessage());
                }
            }

        } else {
            player.sendMessage("Syntax: update <KEY> <VALUE>");
            player.sendMessage("Die folgenden Optionen sind verfügbar:");
            player.sendMessage(this.accountOptionsService.getOptionDefinitionsText());
        }
    }

}
