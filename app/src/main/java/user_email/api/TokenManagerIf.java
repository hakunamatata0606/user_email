package user_email.api;

import java.util.AbstractMap.SimpleEntry;

public interface TokenManagerIf {
    public String generateToken(String username, String email);

    // <username, email>
    public SimpleEntry<String, String> verifyToken(String token) throws Exception;
}
