package de.fantasypixel.rework.modules.sound;

import de.fantasypixel.rework.framework.config.Config;
import de.fantasypixel.rework.framework.log.FPLogger;
import de.fantasypixel.rework.framework.provider.Auto;
import de.fantasypixel.rework.framework.provider.ServiceProvider;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ServiceProvider
public class SoundService {

    @Config private SoundConfig soundConfig;
    @Auto private FPLogger logger;

    /**
     * @param identifier the {@link SoundConfig.SoundMapping#getIdentifier()} of the sound. not save-sensitive!
     * @return whether the sound is registered in the config file
     */
    @Nullable
    private SoundConfig.SoundMapping getSoundMapping(@Nonnull String identifier) {
        return this.soundConfig.getSoundMappings()
                .stream()
                .filter(e -> e.getIdentifier().equalsIgnoreCase(identifier))
                .findFirst()
                .orElse(null);
    }

    /**
     * Plays a sound to the player.
     * @param player the player
     * @param sound the sound (name)
     */
    public void playSound(@Nonnull Player player, @Nonnull Sound sound) {
        var soundData = this.getSoundMapping(sound.name());

        if (soundData == null) {
            this.logger.warning("No sound mapping was found for \"{0}\"!", sound);
            return;
        }

        org.bukkit.Sound bukkitSound;
        org.bukkit.SoundCategory bukkitSoundCategory;

        try {
            bukkitSound = org.bukkit.Sound.valueOf(soundData.getSound());
            bukkitSoundCategory = soundData.getCategory() != null
                ? org.bukkit.SoundCategory.valueOf(soundData.getCategory())
                : SoundCategory.NEUTRAL;
        } catch (IllegalArgumentException ex) {
            this.logger.warning("The sound-mapping {0} contains a wrong enum value. Be sure to check the config/sound file.", sound.name());
            return;
        }

        player.playSound(
                player,
                bukkitSound,
                bukkitSoundCategory,
                1,
                1
        );
    }

}
