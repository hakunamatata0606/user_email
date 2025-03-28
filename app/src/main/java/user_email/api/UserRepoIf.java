package user_email.api;

import user_email.model.User;

public interface UserRepoIf {
    public void createUser(String username, String password, String email) throws Exception;

    public User getUser(String username) throws Exception;

    public void deleteUser(String username) throws Exception;

    public void activateUser(String username) throws Exception;
}
