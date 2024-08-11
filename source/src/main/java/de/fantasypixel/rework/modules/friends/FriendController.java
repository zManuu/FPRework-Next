package de.fantasypixel.rework.modules.friends;

import de.fantasypixel.rework.framework.command.Command;
import de.fantasypixel.rework.framework.provider.Controller;
import de.fantasypixel.rework.framework.provider.Service;
import de.fantasypixel.rework.modules.account.Account;
import de.fantasypixel.rework.modules.account.AccountService;
import de.fantasypixel.rework.modules.notification.NotificationService;
import de.fantasypixel.rework.modules.notification.NotificationType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

@Controller
public class FriendController implements Listener {

    @Service private FriendService friendService;
    @Service private AccountService accountService;
    @Service private NotificationService notificationService;

    @Command(name = "friends")
    public void onFriendsCommand(Player player, String[] args) {

        Account account = this.accountService.getAccount(player.getUniqueId());

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {

            // list friends
            var friends = this.friendService.getFriendsAccounts(account.getId());
            var onlineFriendNames = new HashSet<String>();
            var offlineFriendNames = new HashSet<String>();

            for (Account friendAccount : friends) {
                if (this.accountService.isAccountOnline(friendAccount.getId()))
                    onlineFriendNames.add(friendAccount.getName());
                else
                    offlineFriendNames.add(friendAccount.getName());
            }

            this.notificationService.sendChatMessage(
                    player,
                    "friend-list",
                    Map.of(
                            "COUNT", friends.size(),
                            "ONLINE", "§a" + String.join(", ", onlineFriendNames),
                            "OFFLINE", "§7" + String.join(", ", offlineFriendNames)
                    )
            );

            // show friend requests
            this.friendService.informPlayerAboutOpenFriendRequests(player, account, true);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {

            // send friend-request
            String targetAccountName = args[1];
            Account targetAccount = this.accountService.getAccountByName(targetAccountName);

            if (targetAccount == null) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "unknown-account-name");
                return;
            }

            if (Objects.equals(account.getId(), targetAccount.getId())) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "friend-request-to-self");
                return;
            }

            if (this.friendService.areAccountsFriends(account.getId(), targetAccount.getId())) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "friend-already", targetAccountName);
                return;
            }

            if (this.friendService.existsFriendRequest(account.getId(), targetAccount.getId())) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "friend-request-already", targetAccountName);
                return;
            }

            // other player has sent request to this -> accept
            if (this.friendService.existsFriendRequest(targetAccount.getId(), account.getId())) {
                this.friendService.acceptFriendRequest(account.getId(), targetAccount.getId());
                return;
            }

            // create & send friend request
            if (this.friendService.sendFriendRequest(account.getId(), targetAccount.getId()))
                this.notificationService.sendChatMessage(NotificationType.SUCCESS, player, "friend-request-sent", targetAccountName);
            else
                this.notificationService.sendChatMessage(NotificationType.ERROR, player, "500");

        } else if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {

            // accept friend-request
            String targetAccountName = args[1];
            Account targetAccount = this.accountService.getAccountByName(targetAccountName);

            if (targetAccount == null) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "unknown-account-name");
                return;
            }

            if (Objects.equals(account.getId(), targetAccount.getId())) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "friend-request-to-self");
                return;
            }

            if (this.friendService.areAccountsFriends(account.getId(), targetAccount.getId())) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "friend-already", targetAccountName);
                return;
            }

            if (this.friendService.existsFriendRequest(account.getId(), targetAccount.getId())) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "friend-request-already", targetAccountName);
                return;
            }

            // other player has sent request to this -> accept
            if (this.friendService.existsFriendRequest(targetAccount.getId(), account.getId()))
                this.friendService.acceptFriendRequest(account.getId(), targetAccount.getId());

        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {

            // remove friend
            String targetAccountName = args[1];
            Account targetAccount = this.accountService.getAccountByName(targetAccountName);

            if (targetAccount == null) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "unknown-account-name");
                return;
            }

            if (Objects.equals(account.getId(), targetAccount.getId())) {
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "friend-request-to-self");
                return;
            }

            if (this.friendService.removeFriend(account.getId(), targetAccount.getId()))
                this.notificationService.sendChatMessage(NotificationType.SUCCESS, player, "friend-removed", targetAccountName);
            else
                this.notificationService.sendChatMessage(NotificationType.WARNING, player, "friend-not", targetAccountName);
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        // send a message to all befriended players
        var player = event.getPlayer();
        var account = this.accountService.getAccount(player.getUniqueId());

        if (account == null)
            return;

        var friends = this.friendService.getFriendsAccounts(account.getId());

        for (Account friend : friends) {
            var friendPlayer = this.accountService.getPlayer(friend.getId());
            if (friendPlayer == null)
                continue;

            this.notificationService.sendChatMessage(
                    NotificationType.SUCCESS,
                    friendPlayer,
                    "friend-join",
                    Map.of("FRIEND_NAME", account.getName())
            );
        }

        // list friend-requests (if there are any)
        this.friendService.informPlayerAboutOpenFriendRequests(player, account, false);

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        // send a message to all befriended players
        var account = this.accountService.getAccount(event.getPlayer().getUniqueId());
        var friends = this.friendService.getFriendsAccounts(account.getId());

        for (Account friend : friends) {
            var friendPlayer = this.accountService.getPlayer(friend.getId());
            if (friendPlayer == null)
                continue;

            this.notificationService.sendChatMessage(
                    friendPlayer,
                    "friend-quit",
                    Map.of("FRIEND_NAME", account.getName())
            );
        }

    }

}
