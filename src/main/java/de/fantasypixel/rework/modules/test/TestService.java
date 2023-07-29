package de.fantasypixel.rework.modules.test;

import de.fantasypixel.rework.utils.FPConfig;
import de.fantasypixel.rework.utils.database.DataRepo;
import de.fantasypixel.rework.utils.database.DataRepoProvider;
import de.fantasypixel.rework.utils.provider.Config;
import de.fantasypixel.rework.utils.provider.ServiceProvider;

@ServiceProvider(name = "test-service")
public class TestService {

    @Config
    private FPConfig config;

    @DataRepo(type = TestUser.class)
    private DataRepoProvider<TestUser> userRepo;

    public void test() {
        System.out.println("Testing service...");
        System.out.println("Config: " + config);
        System.out.println("UserRepo: " + userRepo);

        var testUser = new TestUser();
        testUser.setName("manu");

        this.userRepo.insert(testUser);
        System.out.println("inserted new user. resulting id: " + testUser.getId());
    }

}
