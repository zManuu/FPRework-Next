package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.utils.events.OnEnable;
import de.fantasypixel.rework.utils.provider.Controller;
import de.fantasypixel.rework.utils.provider.Service;

@Controller
public class AccountController {

    @Service(name = "account")
    private AccountService accountService;

    @OnEnable
    public void onEnable() {
        this.accountService.sayHello();
    }

}
