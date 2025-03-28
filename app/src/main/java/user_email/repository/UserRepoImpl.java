package user_email.repository;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import user_email.api.UserRepoIf;
import user_email.model.User;

//todo: convert sql exception to specific exception for error handling

public class UserRepoImpl implements UserRepoIf{
    private static final Logger logger = LogManager.getLogger(UserRepoImpl.class);

    private static final String CreateUserSql = "insert into users(username, password, email) values(?, ?, ?);";
    private static final String GetUserSql = "select username, password, email, activated from users where username = ?;";
    private static final String DeleteUserSql = "delete from users where username = ?";
    private static final String ActivateUserSql = "update users set activated = true where username = ?";

    private JdbcTemplate jdbcTemplate;

    public UserRepoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createUser(String username, String password, String email) throws Exception {
        jdbcTemplate.update(CreateUserSql, username, password, email);
    }


    @Override
    public User getUser(String username) throws Exception {
        List<User> users = jdbcTemplate.query(GetUserSql, (rs, rNum) -> {
            return new User(rs.getString("username"), rs.getString("password"), rs.getString("email"), rs.getBoolean("activated"));
        }, username);
        if (users.size() == 0) {
            return null;
        }else if (users.size() > 1) {
            logger.error("Receive multiple users which is not expected");
            throw new Exception("Database error");
        }
        return users.get(0);
    }

    @Override
    public void deleteUser(String username) throws Exception {
        jdbcTemplate.update(DeleteUserSql, username);
    }

    @Override
    public void activateUser(String username) throws Exception {
        jdbcTemplate.update(ActivateUserSql, username);
    }
}
