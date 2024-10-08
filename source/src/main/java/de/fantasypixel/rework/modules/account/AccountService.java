package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.framework.discord.FPDiscordChannel;
import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.database.Query;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.discord.DiscordService;
import de.fantasypixel.rework.modules.events.AccountLoginEvent;
import de.fantasypixel.rework.modules.utils.DateUtils;
import de.fantasypixel.rework.modules.utils.ServerUtils;
import discord4j.rest.util.Color;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@ServiceProvider
public class AccountService {

    private final static String CLASS_NAME = AccountService.class.getSimpleName();

    @Auto private FPLogger logger;
    @DataRepo private DataRepoProvider<Account> accountRepo;
    @Service private DateUtils dateUtils;
    @Service private ServerUtils serverUtils;
    @Service private DiscordService discordService;

    public boolean hasAccount(@Nonnull String playerUuid) {
        return this.accountRepo.exists(new Query("playerUuid", playerUuid));
    }

    @Nullable
    public Account getAccount(@Nonnull String playerUuid) {
        return this.accountRepo.get(new Query("playerUuid", playerUuid));
    }

    @Nullable
    public Account getAccountByName(@Nonnull String name) {
        return this.accountRepo.get(new Query("name", name));
    }

    @Nullable
    public Account getAccount(@Nonnull UUID uuid) {
        return this.getAccount(uuid.toString());
    }

    @Nullable
    public Account getAccount(int accountId) {
        return this.accountRepo.get(new Query("id", accountId));
    }

    @Nullable
    public Player getPlayer(int accountId) {
        for (Player onlinePlayer : this.serverUtils.getOnlinePlayers()) {
            var onlinePlayerAccount = this.getAccount(onlinePlayer.getUniqueId());

            if (Objects.equals(onlinePlayerAccount.getId(), accountId))
                return onlinePlayer;
        }

        return null;
    }

    /**
     * Checks if the given account is online.
     */
    public boolean isAccountOnline(int accountId) {
        var onlinePlayers = this.serverUtils.getOnlinePlayers();
        var onlineAccounts = onlinePlayers.stream()
                .map(player -> this.getAccount(player.getUniqueId()))
                .collect(Collectors.toSet());

        return onlineAccounts.stream()
                .anyMatch(account -> Objects.equals(account.getId(), accountId));
    }

    /**
     * Creates a new account.
     * @param playerUuid the associated player's uuid
     * @param name the associated player's name
     * @param password the password
     * @return the created account or null if an error occurred
     */
    @Nullable
    public Account createAccount(@Nonnull String playerUuid, @Nonnull String name, @Nullable String password) {
        Account account = new Account(null, playerUuid, name, password, null);
        if (!this.accountRepo.insert(account)) {
            this.logger.error(CLASS_NAME, "createAccount", "Couldn't insert account into the database.");
            return null;
        }

        this.discordService.sendEmbed(FPDiscordChannel.LOGS_USER, Color.GREEN, "Account create", "The account {0} was created for player \"{1}\".", account.getId(), name);
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
