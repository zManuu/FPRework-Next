package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.utils.FPConfig;
import de.fantasypixel.rework.utils.database.DataRepo;
import de.fantasypixel.rework.utils.database.DataRepoProvider;
import de.fantasypixel.rework.utils.provider.autorigging.Config;
import de.fantasypixel.rework.utils.provider.autorigging.Plugin;
import de.fantasypixel.rework.utils.provider.ServiceProvider;

@ServiceProvider(name = "account")
public class AccountService {

    @Config private FPConfig config;
    @Plugin private FPRework plugin;

    @DataRepo(type = Account.class)
    private DataRepoProvider<Account> accountRepo;

    public void sayHello() {
        this.plugin.getFpLogger().info("Hello from AccountService!");
    }

    public void testAccountRepo() {
        var account = Account.builder()
                .password("test-password")
                .name("test-name")
                .build();

        if (this.accountRepo.insert(account)) {
            var accountId = account.getId();

            if (accountId == null) {
                this.plugin.getFpLogger().warning("Inserted entity into the database, but the entity-id wasn't updated.");
            } else {
                this.plugin.getFpLogger().info("Successfully inserted an entity into the database. New entity-id: " + accountId);
            }
        } else {
            this.plugin.getFpLogger().warning("Couldn't insert entity into the database.");
        }
    }

    public void logPlayerJoin(String playerName) {
        this.plugin.getFpLogger().info(playerName + " has joined the server!");
    }

    public void logTimerRuns(int runs) {
        this.plugin.getFpLogger().info(
                "The test-timer ran {0} times.",
                runs
        );
    }
}
