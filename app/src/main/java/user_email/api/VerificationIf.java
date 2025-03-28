package user_email.api;

public interface VerificationIf {
    public String createRecord(String username);

    public String verifyRecord(String record);

    public boolean hasRecord(String username);
}
