package user_email.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import user_email.Config;
import user_email.api.EmailIf;

@SpringBootTest
@ContextConfiguration(classes = Config.class)
public class EmailTest {
    
    @Autowired
    private EmailIf emailService;

    @Test
    public void testSendEmail() throws Exception {
        emailService.sendMessage("Test title", "test message", "alohatestmail123@gmail.com");
    }
}
