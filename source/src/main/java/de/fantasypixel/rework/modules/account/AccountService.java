package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.FPConfig;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.provider.autorigging.Config;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import de.fantasypixel.rework.framework.provider.ServiceProvider;

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

    public void logPlayerTestCommand(String[] args) {
        this.plugin.getFpLogger().info(
                "A player has executed the test-command. args={0}",
                String.join(",", args)
        );
    }
}
