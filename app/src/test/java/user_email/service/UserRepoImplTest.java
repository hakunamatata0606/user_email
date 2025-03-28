package user_email.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import user_email.Config;
import user_email.api.UserRepoIf;
import user_email.model.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration(classes = Config.class)
public class UserRepoImplTest {
    @Autowired
    private UserRepoIf userRepo;

    @Test
    public void testUserRepo() throws Exception {
        final String username = "dummy";
        final String password = "dummy";
        final String email = "dummy@example.com";

        userRepo.createUser(username, password, email);

        User user = userRepo.getUser(username);
        assertEquals(username, user.username(), "Expect username same");
        assertEquals(password, user.password(), "Expect password same");
        assertEquals(email, user.email(), "Expect email same");
        assertEquals(false, user.activated(), "Expect username same");

        try {
            userRepo.createUser(username, password, email);
            assertTrue(false, "Expect exception when create existense user");
        }catch (Exception e) {
            
        }

        userRepo.activateUser(username);
        user = userRepo.getUser(username);
        assertEquals(username, user.username(), "Expect username same");
        assertEquals(password, user.password(), "Expect password same");
        assertEquals(email, user.email(), "Expect email same");
        assertEquals(true, user.activated(), "Expect username same");

        user = userRepo.getUser("dummy1");
        assertNull(user);
        userRepo.deleteUser(username);
    }
}
