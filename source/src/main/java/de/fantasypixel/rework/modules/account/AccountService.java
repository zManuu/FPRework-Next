package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.FPRework;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.framework.provider.autorigging.Plugin;
import de.fantasypixel.rework.modules.events.AccountLoginEvent;
import de.fantasypixel.rework.modules.utils.DateUtils;
import de.fantasypixel.rework.modules.utils.ServerUtils;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

@ServiceProvider
public class AccountService {

    private final static String CLASS_NAME = AccountService.class.getSimpleName();

    @Plugin private FPRework plugin;
    @DataRepo private DataRepoProvider<Account> accountRepo;
    @Service private DateUtils dateUtils;
    @Service private ServerUtils serverUtils;

    public boolean hasAccount(String playerUuid) {
        return this.accountRepo.exists("playerUuid", playerUuid);
    }

    public Account getAccount(String playerUuid) {
        return this.accountRepo.get("playerUuid", playerUuid);
    }

    public Account getAccount(UUID uuid) {
        return this.getAccount(uuid.toString());
    }

    public Account createAccount(String playerUuid, String name, String password) {
        Account account = new Account(null, playerUuid, name, password, null);
        if (!this.accountRepo.insert(account)) {
            this.plugin.getFpLogger().error(CLASS_NAME, "createAccount", "Couldn't insert account into the database.");
            return null;
        }
        return account;
    }

    /**
     * Logs in a player into the given account.
     */
    public void login(@Nonnull Player player, @Nonnull Account account) {
        account.setLastLogin(this.dateUtils.getCurrentDateTime());
        this.accountRepo.update(account);
        this.serverUtils.callEvent(new AccountLoginEvent(account, player));
    }
}
