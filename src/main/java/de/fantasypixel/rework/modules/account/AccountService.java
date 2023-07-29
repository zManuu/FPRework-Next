package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.utils.FPConfig;
import de.fantasypixel.rework.utils.database.DataRepo;
import de.fantasypixel.rework.utils.database.DataRepoProvider;
import de.fantasypixel.rework.utils.provider.Config;
import de.fantasypixel.rework.utils.provider.Plugin;
import de.fantasypixel.rework.utils.provider.ServiceProvider;

@ServiceProvider(name = "account")
public class AccountService {

    @Config
    private FPConfig config;

    @DataRepo(type = Account.class)
    private DataRepoProvider<Account> accountRepo;

    @Plugin
    private FPRework plugin;

    public boolean existsAccountWithId(int id) {
        return this.accountRepo.existsWithId(id);
    }

    public boolean createAccount(Account account) {
        return this.accountRepo.insert(account);
    }

    public void sayHello() {
        this.plugin.getLogger().info("Hello from AccountService!");
    }
}
