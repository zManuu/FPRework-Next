package de.fantasypixel.rework.modules.account.options;

import de.fantasypixel.rework.framework.config.ConfigProvider;
import lombok.Getter;

@Getter
@ConfigProvider(path = "config/account-options")
public class AccountOptionsConfig {

    private AccountOptions defaultOptions;

}
