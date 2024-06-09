package de.fantasypixel.rework.framework.discord;

import de.fantasypixel.rework.FPRework;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;

/**
 * Manages the discord-integration.
 * @see <a href="https://mvnrepository.com/artifact/com.discord4j/discord4j-core">Discord4J</a>
 */
public class DiscordManager {

    @Getter private FPDiscordClient fpDiscordClient;
    private GatewayDiscordClient discordClient;
    private final FPRework plugin;
    private DiscordConfig discordConfig;

    public DiscordManager(@Nonnull FPRework plugin, @Nullable DiscordConfig discordConfig) {
        this.plugin = plugin;

        if (discordConfig == null) {
            this.plugin.getFpLogger().warning("Didn't find a discord-config. Should be located at config/discord.json!");
            return;
        }

        this.discordConfig = discordConfig;
        this.initDiscordClient();
    }

    /**
     * Initializes the discord-client.
     * <br>
     * <b>Note:</b> this method pauses the thread until the discord-client is connected!
     */
    public void initDiscordClient() {
        this.fpDiscordClient = new FPDiscordClient() {
            @Override
            public void sendMessage(@Nonnull FPDiscordChannel channel, @Nonnull String message) {
                var channelId = discordConfig.getChannelMap().get(channel);

                if (channelId == null) {
                    plugin.getFpLogger().warning("Tried to send discord-message to channel {0}, but the channel wasn't properly configured in the discord.json!", channel);
                    return;
                }

                if (discordClient == null) {
                    plugin.getFpLogger().warning("Tried to send discord-message, but the discord-client was not setup yet!");
                    return;
                }

                // send message to channel
                discordClient.getChannelById(Snowflake.of(channelId))
                        .ofType(MessageChannel.class)
                        .flatMap(messageChannel -> messageChannel.createMessage(message))
                        .subscribe();

            }
            @Override
            public void sendMessage(@Nonnull FPDiscordChannel channel, @Nonnull String pattern, @Nullable Object... args) {
                this.sendMessage(channel, MessageFormat.format(pattern, args));
            }
        };

        var discordClientToken = this.plugin.getFpUtils().getEnvironmentVar("FP_NEXT_DISCORD_CLIENT_TOKEN");

        if (discordClientToken.isEmpty()) {
            this.plugin.getFpLogger().warning("No discord-client-token was found in the environment variables (FP_NEXT_DISCORD_CLIENT_TOKEN). Please add one!");
            return;
        }

        DiscordClient.create(discordClientToken.get())
                .gateway()
                .setEnabledIntents(IntentSet.of(
                        Intent.GUILD_MESSAGES,
                        Intent.GUILD_MESSAGE_REACTIONS,
                        Intent.GUILD_MEMBERS,
                        Intent.DIRECT_MESSAGES,
                        Intent.MESSAGE_CONTENT
                ))
                .login()
                .subscribe(client -> {
                    this.plugin.getFpLogger().debug("Connected to discord-client {0}.", client.getSelfId().asString());

                    client.getEventDispatcher().on(ReadyEvent.class)
                            .subscribe(event -> {
                                discordClient = client;
                                User self = event.getSelf();
                                this.plugin.getFpLogger().debug("Logged in as {0}#{1}.", self.getUsername(), self.getDiscriminator());
                            });

                    // client.getEventDispatcher().on(MessageCreateEvent.class)
                    //         .map(MessageCreateEvent::getMessage)
                    //         .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
                    //         .filter(message -> message.getContent().equalsIgnoreCase("!ping"))
                    //         .flatMap(Message::getChannel)
                    //         .flatMap(channel -> channel.createMessage("Pong!"))
                    //         .subscribe();
                });

        this.plugin.getFpLogger().debug("Waiting for discord-client initialization...");
        while (this.discordClient == null) {
            Thread.yield();
        }
        this.plugin.getFpLogger().debug("The discord-client was initialized.");
    }

    /**
     * Stops the discord-bot.
     */
    public void stop() {
        this.plugin.getFpLogger().info("Stopping discord-bot.");
        this.discordClient.logout().block();
    }

}
