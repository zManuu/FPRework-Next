package de.fantasypixel.rework.framework.provider;

import de.fantasypixel.rework.framework.log.FPLogger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Manages the connection to the Citizens plugin (only to listen to the enable event).
 * Note that this is a {@link Listener} and therefore needs to be registered to work.
 */
@AllArgsConstructor
public class CitizensManager implements Listener {

    @Getter private boolean enabled;
    private FPLogger logger;

    @EventHandler
    public void onCitizensEnabled(CitizensEnableEvent event) {
        this.logger.debug("The Citizens API is now enabled.");
        this.enabled = true;
    }

}
