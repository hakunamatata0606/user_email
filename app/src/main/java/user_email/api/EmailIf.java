package user_email.api;

public interface EmailIf {
    public void sendMessage(String title, String message, String email) throws Exception;
}
