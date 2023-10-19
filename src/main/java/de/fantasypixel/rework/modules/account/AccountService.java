package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.utils.FPConfig;
import de.fantasypixel.rework.utils.database.DataRepo;
import de.fantasypixel.rework.utils.database.DataRepoProvider;
import de.fantasypixel.rework.utils.provider.Config;
import de.fantasypixel.rework.utils.provider.Plugin;
import de.fantasypixel.rework.utils.provider.ServiceProvider;
import org.bukkit.event.player.PlayerJoinEvent;

@ServiceProvider(name = "account")
public class AccountService {

    @Config private FPConfig config;
    @Plugin private FPRework plugin;

    @DataRepo(type = Account.class)
    private DataRepoProvider<Account> accountRepo;

    public void sayHello() {
        this.plugin.getLogger().info("Hello from AccountService!");
    }

    public void testAccountRepo() {
        var account = Account.builder()
                .password("test-password")
                .name("test-name")
                .build();

        if (this.accountRepo.insert(account)) {
            var accountId = account.getId();

            if (accountId == null) {
                this.plugin.getLogger().warning("Inserted entity into the database, but the entity-id wasn't updated.");
            } else {
                this.plugin.getLogger().info("Successfully inserted an entity into the database. New entity-id: " + accountId);
            }
        } else {
            this.plugin.getLogger().warning("Couldn't insert entity into the database.");
        }
    }

    public void logPlayerJoin(String playerName) {
        this.plugin.getLogger().info(playerName + " has joined the server!");
    }
}
