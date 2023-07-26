package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.utils.modules.Service;

public class AccountService extends Service<AccountConfig, AccountRepo> {

    public AccountService(AccountConfig config, AccountRepo entityRepo) {
        super(config, entityRepo);
    }

}
