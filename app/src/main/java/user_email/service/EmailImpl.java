package user_email.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import user_email.api.EmailIf;

public class EmailImpl implements EmailIf {

    private final JavaMailSender javaMailSender;

    private final String serverEmail;

    public EmailImpl(JavaMailSender javaMailSender, String serverEmail) {
        this.javaMailSender = javaMailSender;
        this.serverEmail = serverEmail;
    }

    @Override
    public void sendMessage(String title, String message, String email) throws Exception{
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(this.serverEmail);
        mail.setTo(email);
        mail.setSubject(title);
        mail.setText(message);
        javaMailSender.send(mail);
    }
}
