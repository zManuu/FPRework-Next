package de.fantasypixel.rework.framework.discord;

import discord4j.core.spec.EmbedCreateSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface FPDiscordClient {

    void sendMessage(@Nonnull FPDiscordChannel channel, @Nonnull String message);
    void sendMessage(@Nonnull FPDiscordChannel channel, @Nonnull String pattern, @Nullable Object... args);
    void sendEmbed(@Nonnull FPDiscordChannel channel, @Nonnull EmbedCreateSpec embedCreateSpec);

}
