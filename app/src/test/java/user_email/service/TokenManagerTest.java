package user_email.service;

import org.junit.jupiter.api.Test;

import com.auth0.jwt.exceptions.TokenExpiredException;

import user_email.api.TokenManagerIf;

import static org.junit.jupiter.api.Assertions.*;

import java.util.AbstractMap.SimpleEntry;

public class TokenManagerTest {
    
    @Test
    public void tokenMangerTest() throws Exception {
        final String user = "bao";
        final String email = "bao@mail.com";

        TokenManagerIf tokenManager = new TokenManagerImpl("secret", 2);
        String token = tokenManager.generateToken(user, email);
        SimpleEntry<String, String> entry = tokenManager.verifyToken(token);
  
        assertEquals(user, entry.getKey());
        assertEquals(email, entry.getValue());

        Thread.sleep(3000);

        try {
            entry = tokenManager.verifyToken(token);
            assertTrue(false);
        }catch (Exception e) {
            assertTrue(e instanceof TokenExpiredException);
        }
    }
}
