package user_email.service;

import org.apache.catalina.connector.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import user_email.api.PasswordEncoderIf;
import user_email.api.TokenManagerIf;
import user_email.api.UserRepoIf;
import user_email.model.User;

// todo: handle specific error
@RestController
public class UserService {
    public static record UserRequest(String username, String password, String email) {}

    public static record UserLoginRequest(String username, String password) {}
    
    public static record UserLoginResponse(String token, boolean activated) {}

    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    private UserRepoIf userRepo;

    @Autowired
    private PasswordEncoderIf passwordEncoder;

    @Autowired
    private TokenManagerIf tokenManager;

    @Autowired
    private EmailHandler emailHandler;

    @PostMapping(path = "/v1/user")
    public ResponseEntity<Void> createUser(@RequestBody UserRequest userRequest) {
        String passwordEncoded = passwordEncoder.encode(userRequest.password());
        logger.debug("Got request create user:", userRequest);
        try {
            userRepo.createUser(userRequest.username(), passwordEncoded, userRequest.email());
        }catch (Exception e) {
            logger.error("Fail to create user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        logger.debug("Create user successfully");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/v1/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest userLoginRequest) {
        User userDb = null;
        logger.debug("Receive login request: ", userLoginRequest);
        try {
            userDb = userRepo.getUser(userLoginRequest.username());
        }catch (Exception e) {
            logger.error("Fail to get user: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        if (userDb == null) {
            logger.debug("Not found user: ", userLoginRequest);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!passwordEncoder.verify(userLoginRequest.password(), userDb.password())) {
            logger.debug("Password does not match: ", userLoginRequest);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String token = tokenManager.generateToken(userDb.username(), userDb.email());
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(new UserLoginResponse(token, userDb.activated()));
    }

    @GetMapping("/v1/protected")
    public String protectedData(@RequestAttribute("username") String username, @RequestAttribute("email") String email) {
        return "This is protected: " + username + " " + email;
    }

    @PutMapping("/v1/verifyUser")
    public ResponseEntity<Void> verifyUser(@RequestAttribute("username") String username, @RequestAttribute("email") String email) {
        emailHandler.sendVerificationEmail(username, email);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/v1/verification")
    public ResponseEntity<Void> userSendVerification(@RequestParam(required = true) String id) {
        if (id.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String username = emailHandler.verifyAccount(id);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            userRepo.activateUser(username);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
