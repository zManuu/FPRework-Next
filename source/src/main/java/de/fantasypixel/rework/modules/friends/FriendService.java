package de.fantasypixel.rework.modules.friends;

import de.fantasypixel.rework.framework.database.DataRepo;
import de.fantasypixel.rework.framework.database.DataRepoProvider;
import de.fantasypixel.rework.framework.database.Query;
import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import de.fantasypixel.rework.modules.account.Account;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.notification.NotificationType;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ServiceProvider
public class FriendService {

    @Auto private FPLogger logger;
    @DataRepo private DataRepoProvider<Friend> friendsRepo;
    @DataRepo private DataRepoProvider<FriendRequest> friendRequestsRepo;
    @Service private AccountService accountService;
    @Service private NotificationService notificationService;

    /**
     * Gets all friends of the specified account. This performs a bidirectional check meaning both accountId1 and accountId2 are checked.
     * @param accountId the account to get all friends of
     * @return all friends (no matter online or offline)
     */
    public Set<Account> getFriendsAccounts(int accountId) {
        var friends1 = this.friendsRepo.getMultiple(new Query("accountId1", accountId));
        var friends2 = this.friendsRepo.getMultiple(new Query("accountId2", accountId));

        var allFriends = new HashSet<Account>();

        allFriends.addAll(
                friends1.stream()
                        .map(friend -> this.accountService.getAccount(friend.getAccountId2()))
                        .collect(Collectors.toSet())
        );

        allFriends.addAll(
                friends2.stream()
                        .map(friend -> this.accountService.getAccount(friend.getAccountId1()))
                        .collect(Collectors.toSet())
        );

        return allFriends;
    }

    /**
     * Checks whether 2 accounts are befriended. This performs a bidirectional check meaning both accountId1 and accountId2 are checked.
     * @param accountId1 the first account id
     * @param accountId2 the second account id
     * @return whether the two accounts are befriended
     */
    public boolean areAccountsFriends(int accountId1, int accountId2) {
        var query1 = new Query()
                .where("accountId1", accountId1)
                .where("accountId2", accountId2);

        var query2 = new Query()
                .where("accountId2", accountId2)
                .where("accountId1", accountId1);

        return this.friendsRepo.exists(query1) || this.friendsRepo.exists(query2);
    }

    /**
     * Checks whether a friend request exists from account A to B.
     */
    public boolean existsFriendRequest(int requestingAccountId, int receivingAccountId) {
        return this.friendRequestsRepo.exists(
                new Query()
                        .where("requestingAccountId", requestingAccountId)
                        .where("receivingAccountId", receivingAccountId)
        );
    }

    /**
     * Sends a friend request. Before calling this, make sure no friend request exists and the accounts aren't friends already. If the targeted player is online, they will be notified.
     * @param accountId the account to send the friend request
     * @param otherAccountId the account to receive the friend request
     * @return whether the friend request was send
     */
    public boolean sendFriendRequest(int accountId, int otherAccountId) {
        var friendRequest = new FriendRequest(
                null,
                accountId,
                otherAccountId
        );

        if (this.friendRequestsRepo.insert(friendRequest)) {
            this.logger.debug("Send a friend request from {0} to {1}.", accountId, otherAccountId);

            String sendingPlayerName = this.accountService.getAccount(accountId).getName();

            // send message to receiving player (if they are online)
            Player player = this.accountService.getPlayer(otherAccountId);
            if (player != null)
                this.notificationService.sendChatMessage(NotificationType.SUCCESS, player, "friend-request-received", Map.of("PLAYER_NAME", sendingPlayerName));

            return true;
        }

        this.logger.warning("Couldn't send friend request from {0} to {1}.", accountId, otherAccountId);
        return false;
    }

    /**
     * Gets all open friend requests the given account has received.
     */
    @Nonnull
    public Set<FriendRequest> getOpenFriendRequests(int accountId) {
        return this.friendRequestsRepo
                .getMultiple(new Query("receivingAccountId", accountId));
    }

    /**
     * Accepts a friend request.
     * @param accountId the account that received the request
     * @param otherAccountId the account that sent the request
     */
    public void acceptFriendRequest(int accountId, int otherAccountId) {

        // delete friend-request
        var friendRequest = this.friendRequestsRepo.get(
                new Query()
                        .where("requestingAccountId", otherAccountId)
                        .where("receivingAccountId", accountId)
        );
        if (friendRequest == null) {
            this.logger.warning("Tried to accept the friend request <{0}->{1}>, but none found.", otherAccountId, accountId);
            return;
        }
        this.friendRequestsRepo.delete(friendRequest);

        // check if players are friends
        if (this.areAccountsFriends(accountId, otherAccountId)) {
            this.logger.warning("Tried to accept the friend request <{0}->{1}>, but the players are friends already.", otherAccountId, accountId);
            return;
        }

        // create friend entry
        var friend = new Friend(
                null,
                accountId,
                otherAccountId
        );

        if (this.friendsRepo.insert(friend)) {
            this.logger.debug("Created a friend-entry for {0} & {1}.", accountId, otherAccountId);

            Player player = this.accountService.getPlayer(accountId);
            Player otherPlayer = this.accountService.getPlayer(otherAccountId);

            String playerName = this.accountService.getAccount(accountId).getName();
            String otherPlayerName = this.accountService.getAccount(otherAccountId).getName();

            if (player != null)
                this.notificationService.sendChatMessage(NotificationType.SUCCESS, player, "friend-request-accepted", Map.of("PLAYER_NAME", otherPlayerName));

            if (otherPlayer != null)
                this.notificationService.sendChatMessage(NotificationType.SUCCESS, otherPlayer, "friend-request-accepted", Map.of("PLAYER_NAME", playerName));
        }
        else
            this.logger.warning("Couldn't insert friend-entry for {0} & {1}.", accountId, otherAccountId);
    }

    /**
     * Informs the given player about friend-requests they have received.
     * @param player the player
     * @param account the player's account
     * @param force when set to false and no friend-requests were received, no message will be sent
     */
    public void informPlayerAboutOpenFriendRequests(@Nonnull Player player, @Nonnull Account account, boolean force) {
        var friendRequests = this.getOpenFriendRequests(account.getId());

        if (!force && friendRequests.isEmpty())
            return;

        this.notificationService.sendChatMessage(
                player,
                "friend-requests",
                Map.of(
                        "COUNT",
                        friendRequests.size(),
                        "NAMES",
                        String.join(
                                ", ",
                                friendRequests.stream()
                                        .map(friendRequest -> this.accountService.getAccount(friendRequest.getRequestingAccountId()))
                                        .filter(Objects::nonNull)
                                        .map(Account::getName)
                                        .toArray(String[]::new)
                        )
                )
        );
    }

    /**
     * Removed a friend-entry. This performs a bidirectional check.
     * @param accountId the players account-id
     * @param otherAccountId the friends account-id
     * @return false, if the 2 accounts aren't friends
     */
    public boolean removeFriend(int accountId, int otherAccountId) {
        var query1 = new Query()
                .where("accountId1", accountId)
                .where("accountId2", otherAccountId);

        var query2 = new Query()
                .where("accountId2", accountId)
                .where("accountId1", otherAccountId);

        Friend friend = this.friendsRepo.get(query1);
        if (friend == null)
            friend = this.friendsRepo.get(query2);

        if (friend == null) {
            this.logger.debug("Tried to remove friend <{0}->{1}>, but no record found.", accountId, otherAccountId);
            return false;
        }

        this.friendsRepo.delete(friend);
        this.logger.debug("Deleted friend-entry <{0}->{1}>", accountId, otherAccountId);
        return true;
    }

}
