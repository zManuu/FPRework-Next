package de.fantasypixel.rework.modules.discord;

import de.fantasypixel.rework.framework.discord.FPDiscordChannel;
import de.fantasypixel.rework.framework.discord.FPDiscordClient;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;

/**
 * A simple service forwarding methods from {@link FPDiscordClient} & some new ones for easier access.
 */
@ServiceProvider
public class DiscordService {

    @Auto private FPDiscordClient discordClient;

    public void sendMessage(@Nonnull FPDiscordChannel channel, @Nonnull String message) {
        this.discordClient.sendMessage(channel, message);
    }

    public void sendMessage(@Nonnull FPDiscordChannel channel, @Nonnull String pattern, @Nullable Object... args) {
        this.discordClient.sendMessage(channel, pattern, args);
    }

    public void sendEmbed(@Nonnull FPDiscordChannel channel, @Nonnull EmbedCreateSpec embedCreateSpec) {
        this.discordClient.sendEmbed(channel, embedCreateSpec);
    }

    public void sendEmbed(@Nonnull FPDiscordChannel channel, @Nonnull Color color, @Nonnull String title, @Nonnull String message) {
        this.discordClient.sendEmbed(
                channel,
                EmbedCreateSpec.builder()
                        .color(color)
                        .title(title)
                        .description(message)
                        .build()
        );
    }

    public void sendEmbed(@Nonnull FPDiscordChannel channel, @Nonnull Color color, @Nonnull String title, @Nonnull String pattern, @Nullable Object...args) {
        this.sendEmbed(
                channel,
                color,
                title,
                MessageFormat.format(pattern, args)
        );
    }

    public void sendEmbed(@Nonnull FPDiscordChannel channel, @Nonnull Color color, @Nonnull String title) {
        this.discordClient.sendEmbed(
                channel,
                EmbedCreateSpec.builder()
                        .color(color)
                        .title(title)
                        .build()
        );
    }

}
