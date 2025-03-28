package user_email.service;

import java.security.MessageDigest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import user_email.api.PasswordEncoderIf;

public class PasswordEncoderMd5 implements PasswordEncoderIf{
    private static final Logger logger = LogManager.getLogger(PasswordEncoderMd5.class);

    private final String secret;

    public PasswordEncoderMd5(String secret) {
        this.secret = secret;
    }
    @Override
    public String encode(String password) {
        String data = password + this.secret;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("md5");
        }catch (Exception e) {
            logger.error("Failed to get md5 digest", e);
            assert false;
        }
        md.update(data.getBytes());
        StringBuilder builder = new StringBuilder();
        for (byte b: md.digest()) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    @Override
    public boolean verify(String password, String encoded) {
        return encoded.equals(encode(password));
    }
}
