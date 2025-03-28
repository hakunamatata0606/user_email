package user_email.service;

import org.junit.jupiter.api.Test;

import user_email.api.PasswordEncoderIf;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordEncoderMd5Test {
    @Test
    public void testEncoder() {
        final String secret = "aloha";
        final String password = "this is password";
        final String expectedEncoded = "787d90d9ee89c9991b124809653136bf";
        final String password1 = "dummy";
        final String expectedEncoded1 = "3dde9ab1ae8e2a7c346e0a7c602eab87";

        PasswordEncoderIf encoder = new PasswordEncoderMd5(secret);
        String encoded = encoder.encode(password);
        assertEquals(expectedEncoded, encoded);
        assertTrue(encoder.verify(password, encoded));
    
        encoded = encoder.encode(password1);
        assertEquals(expectedEncoded1, encoded);
        assertTrue(encoder.verify(password1, encoded));
    }
}
