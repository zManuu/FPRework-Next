package de.fantasypixel.rework.modules.test;

import de.fantasypixel.rework.utils.FPConfig;
import de.fantasypixel.rework.utils.provider.Config;
import de.fantasypixel.rework.utils.provider.ServiceProvider;

@ServiceProvider(name = "test-service")
public class TestService {

    @Config
    public FPConfig config;

}
