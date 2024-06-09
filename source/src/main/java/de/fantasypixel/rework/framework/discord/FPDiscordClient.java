package de.fantasypixel.rework.framework.discord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface FPDiscordClient {

    void sendMessage(@Nonnull FPDiscordChannel channel, @Nonnull String message);
    void sendMessage(@Nonnull FPDiscordChannel channel, @Nonnull String pattern, @Nullable Object... args);

}
