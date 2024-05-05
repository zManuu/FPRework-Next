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
import javax.annotation.Nullable;
import java.util.UUID;

@ServiceProvider
public class AccountService {

    private final static String CLASS_NAME = AccountService.class.getSimpleName();

    @Plugin private FPRework plugin;
    @DataRepo private DataRepoProvider<Account> accountRepo;
    @Service private DateUtils dateUtils;
    @Service private ServerUtils serverUtils;

    public boolean hasAccount(@Nonnull String playerUuid) {
        return this.accountRepo.exists("playerUuid", playerUuid);
    }

    @Nullable
    public Account getAccount(@Nonnull String playerUuid) {
        return this.accountRepo.get("playerUuid", playerUuid);
    }

    @Nullable
    public Account getAccount(@Nonnull UUID uuid) {
        return this.getAccount(uuid.toString());
    }

    @Nullable
    public Account createAccount(@Nonnull String playerUuid, @Nonnull String name, @Nullable String password) {
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
