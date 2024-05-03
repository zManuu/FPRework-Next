package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;

@ServiceProvider
public class AccountService {

    private final static String CLASS_NAME = AccountService.class.getSimpleName();

    @Plugin private FPRework plugin;
    @DataRepo private DataRepoProvider<Account> accountRepo;

    public boolean hasAccount(String playerUuid) {
        return this.accountRepo.exists("playerUuid", playerUuid);
    }

    public Account getAccount(String playerUuid) {
        return this.accountRepo.get("playerUuid", playerUuid);
    }

    public Account createAccount(String playerUuid, String name, String password) {
        Account account = new Account(null, playerUuid, name, password, null);
        if (!this.accountRepo.insert(account)) {
            this.plugin.getFpLogger().error(CLASS_NAME, "createAccount", "Couldn't insert account into the database.");
            return null;
        }
        return account;
    }

}
