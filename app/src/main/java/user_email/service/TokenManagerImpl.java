package user_email.service;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import user_email.api.TokenManagerIf;

public class TokenManagerImpl implements TokenManagerIf {
    private final String secret;
    private final int expireTime; // seconds

    public TokenManagerImpl(String secret, int expireTime) {
        this.secret = secret;
        this.expireTime = expireTime;
    }

    @Override
    public String generateToken(String username, String email) {
        Instant now = Instant.now();
        Instant expired = now.plusSeconds(this.expireTime);
    
        Algorithm algorithm = Algorithm.HMAC256(this.secret);
        String token = JWT.create()
            .withSubject(username)
            .withIssuedAt(now)
            .withExpiresAt(expired)
            .withClaim("email", email)
            .sign(algorithm);
        return token;
    }

    @Override
    public SimpleEntry<String, String> verifyToken(String token) throws Exception {
        Algorithm algorithm = Algorithm.HMAC256(this.secret);
        DecodedJWT decodedJWT = JWT.require(algorithm)
            .build()
            .verify(token);
        String email = decodedJWT.getClaim("email").asString();
        if (email == null) {
            throw new MissingClaimException("email");
        }
        return new SimpleEntry<>(decodedJWT.getSubject(), email);
    }

    
}
