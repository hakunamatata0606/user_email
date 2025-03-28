package user_email.api;

public interface PasswordEncoderIf {
    String encode(String password);

    boolean verify(String password, String encoded);
}
