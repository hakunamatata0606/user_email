package user_email.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import user_email.api.EmailIf;
import user_email.api.VerificationIf;

@Service
public class EmailHandler {

    private static final Logger logger = LogManager.getLogger(EmailHandler.class);
    
    @Autowired
    private EmailIf emailHandler;

    @Autowired
    private VerificationIf verificationImpl;

    @Value("${server.address:localhost}")
    private String serverAddress;

    @Value("${server.port:8080}")
    private String serverPort;

    @Async
    public void sendVerificationEmail(String username, String toEmail) {
        final String id = verificationImpl.createRecord(username);
        final String message = String.format("Please click this link to verify your account: http://%s:%s/v1/verification?id=%s", serverAddress, serverPort, id);
        try {
            emailHandler.sendMessage("Verify your account", message, toEmail);
        }catch (Exception e) {
            // todo: handle error and retry
            logger.error("Failed to send email: ", e);
        }
    }

    public String verifyAccount(String id) {
        return verificationImpl.verifyRecord(id);
    }
}
