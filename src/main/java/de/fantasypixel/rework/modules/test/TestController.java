package de.fantasypixel.rework.modules.test;

import de.fantasypixel.rework.utils.provider.Controller;
import de.fantasypixel.rework.utils.provider.Service;

@Controller
public class TestController {

    @Service(name = "test-service")
    public TestService service;

}
