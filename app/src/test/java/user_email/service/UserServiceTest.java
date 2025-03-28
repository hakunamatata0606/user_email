package user_email.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import user_email.Config;
import user_email.api.PasswordEncoderIf;
import user_email.api.UserRepoIf;
import user_email.api.VerificationIf;
import user_email.model.User;
import user_email.service.UserService.UserLoginRequest;
import user_email.service.UserService.UserLoginResponse;
import user_email.service.UserService.UserRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
@ComponentScan(basePackages = "user_email.service")
@ContextConfiguration(classes = Config.class)
public class UserServiceTest {
    private static final Logger logger = LogManager.getLogger(UserServiceTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepoIf userRepo;

    @Autowired
    private PasswordEncoderIf passwordEncoder;

    @Autowired
    private VerificationIf verificationService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testUserService() throws Exception{
        final UserRequest userRequest = new UserRequest("dummy", "password", "dummy@example.com");
        final String userRequestJson = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(post("/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userRequestJson)
        ).andExpect(status().isCreated());

        User user = userRepo.getUser(userRequest.username());
        assertEquals(userRequest.username(), user.username());
        assertEquals(userRequest.email(), user.email());
        assertTrue(passwordEncoder.verify(userRequest.password(), user.password()));

        userRepo.deleteUser(userRequest.username());
        user = userRepo.getUser(userRequest.username());
        assertNull(user);
    }

    @Test
    public void testLoginService() throws Exception {
        final UserRequest userRequest = new UserRequest("user", "password", "dummy@example.com");
        final String userRequestJson = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(post("/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userRequestJson)
        ).andExpect(status().isCreated());

        User user = userRepo.getUser(userRequest.username());
        assertEquals(userRequest.username(), user.username());
        assertEquals(userRequest.email(), user.email());
        assertTrue(passwordEncoder.verify(userRequest.password(), user.password()));

        UserLoginRequest userLoginRequest = new UserLoginRequest(userRequest.username(), userRequest.password());
        String userLoginRequestJson = objectMapper.writeValueAsString(userLoginRequest);
    
        mockMvc.perform(post("/v1/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userLoginRequestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.activated").value(false));

        userLoginRequest = new UserLoginRequest("dummy", "dummy");
        userLoginRequestJson = objectMapper.writeValueAsString(userLoginRequest);
        mockMvc.perform(post("/v1/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userLoginRequestJson))
            .andExpect(status().isBadRequest());

        userRepo.deleteUser(userRequest.username());
        user = userRepo.getUser(userRequest.username());
        assertNull(user);
    }

    @Test
    public void testEmailService() throws Exception {
        final UserRequest userRequest = new UserRequest("user", "password", "alohatestmail123@gmail.com");
        final String userRequestJson = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(post("/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userRequestJson)
        ).andExpect(status().isCreated());

        User user = userRepo.getUser(userRequest.username());
        assertEquals(userRequest.username(), user.username());
        assertEquals(userRequest.email(), user.email());
        assertTrue(passwordEncoder.verify(userRequest.password(), user.password()));
        assertFalse(user.activated());

        UserLoginRequest userLoginRequest = new UserLoginRequest(userRequest.username(), userRequest.password());
        String userLoginRequestJson = objectMapper.writeValueAsString(userLoginRequest);
    
        String reponseString = mockMvc.perform(post("/v1/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(userLoginRequestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.activated").value(false))
            .andReturn()
            .getResponse()
            .getContentAsString();

        UserLoginResponse response = objectMapper.readValue(reponseString, UserLoginResponse.class);
        assertNotNull(response);
        mockMvc.perform(put("/v1/verifyUser")
            .header("Authorization", "Bearer " + response.token()))
            .andExpect(status().isOk());

        assertTrue(verificationService.hasRecord(userRequest.username()));
        // this would give the same record sent to mail
        String id = verificationService.createRecord(userRequest.username());
        assertNotNull(id);
        mockMvc.perform(get("/v1/verification")
            .param("id", id))
            .andExpect(status().isOk());
        user = userRepo.getUser(userRequest.username());
        assertEquals(userRequest.username(), user.username());
        assertEquals(userRequest.email(), user.email());
        assertTrue(passwordEncoder.verify(userRequest.password(), user.password()));
        assertTrue(user.activated());

        userRepo.deleteUser(userRequest.username());
        user = userRepo.getUser(userRequest.username());
        assertNull(user);
    }
}
