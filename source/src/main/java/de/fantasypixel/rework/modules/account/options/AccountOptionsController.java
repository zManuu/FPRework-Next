package de.fantasypixel.rework.modules.account.options;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.language.LanguageService;
import org.bukkit.entity.Player;

@Controller
public class AccountOptionsController {

    @Service private AccountOptionsService accountOptionsService;
    @Service private AccountService accountService;
    @Service private LanguageService languageService;

    @Command(name = "options", aliases = {"settings", "account"})
    public void optionsCommand(Player player, String[] args) {
        var account = this.accountService.getAccount(player.getUniqueId());
        var accountId = account.getId();

        if (args.length == 0) {

            var optionsText = this.accountOptionsService.getOptionsText(accountId);
            player.sendMessage(this.languageService.getTranslation(account.getId(), "options-list", optionsText));

        } else if (args.length == 3 && args[0].equalsIgnoreCase("update")) {
            var optionKey = args[1];
            var optionValue = args[2];

            try {
                var updateSuccess = this.accountOptionsService.updateOptions(accountId, optionKey, optionValue);

                if (!updateSuccess) {
                    player.sendMessage(this.languageService.getTranslation(accountId, "options-update-error"));
                    throw new IllegalArgumentException("Options couldn't be updated properly.");
                } else
                    player.sendMessage(this.languageService.getTranslation(accountId, "options-update-success"));

            } catch (IllegalArgumentException ex) {
                if (ex.getMessage() == null) {
                    // invalid option
                    player.sendMessage(this.languageService.getTranslation(accountId, "options-invalid-option", this.accountOptionsService.getOptionDefinitionsText()));
                } else {
                    // invalid value
                    player.sendMessage(this.languageService.getTranslation(accountId, "options-invalid-value", ex.getMessage()));
                }
            }

        } else {
            player.sendMessage(this.languageService.getTranslation(accountId, "options-invalid-syntax"));
        }
    }

}
