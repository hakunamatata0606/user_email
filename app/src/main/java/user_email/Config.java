package user_email;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import user_email.api.EmailIf;
import user_email.api.PasswordEncoderIf;
import user_email.api.TokenManagerIf;
import user_email.api.UserRepoIf;
import user_email.api.VerificationIf;
import user_email.repository.UserRepoImpl;
import user_email.service.AuthorizationMiddleware;
import user_email.service.EmailImpl;
import user_email.service.PasswordEncoderMd5;
import user_email.service.TokenManagerImpl;
import user_email.service.VerificationImpl;

@Configuration
@ComponentScan(basePackages = "user_email.service")
public class Config {
    @Value("${database.driverclassname}")
    private String dbDriverClassName;

    @Value("${database.url}")
    private String dbUrl;

    @Value("${database.username}")
    private String dbUsername;

    @Value("${database.password}")
    private String dbPassword;

    @Value("${server.secret}")
    private String serverSecret;

    @Value("${server.token_timeout}")
    private int tokenTimeout;

    @Value("${server.email_timeout}")
    private int emailTimeout;

    @Value("${spring.mail.host}")
    private String emailHost;

    @Value("${spring.mail.port}")
    private int emailPort;

    @Value("${spring.mail.username}")
    private String serverEmail;

    @Value("${spring.mail.password}")
    private String serverEmailPassword;

    @Value("${spring.mail.protocol}")
    private String mailProtocol;

    @Value("${spring.mail.smtp.auth}")
    private String mailAuth;

    @Value("${spring.mail.smtp.starttls.enable}")
    private String mailStartTls;

    @Value("${spring.mail.default-encoding}")
    private String mailEncoding;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(this.dbDriverClassName);
        dataSource.setUrl(this.dbUrl);
        dataSource.setUsername(this.dbUsername);
        dataSource.setPassword(this.dbPassword);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public UserRepoIf userRepo(JdbcTemplate jdbcTemplate) {
        return new UserRepoImpl(jdbcTemplate);
    }

    @Bean
    public PasswordEncoderIf passwordEncoder() {
        return new PasswordEncoderMd5(this.serverSecret);
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(emailHost);
        javaMailSender.setPort(emailPort);
        javaMailSender.setUsername(serverEmail);
        javaMailSender.setPassword(serverEmailPassword);
        javaMailSender.setProtocol(mailProtocol);
        javaMailSender.setDefaultEncoding(mailEncoding);
        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", mailAuth);
        properties.put("mail.smtp.starttls.enable", mailStartTls);

        return javaMailSender;
    }

    @Bean
    public EmailIf emailService(JavaMailSender javaMailSender) {
        return new EmailImpl(javaMailSender, serverEmail);
    }

    @Bean
    public TokenManagerIf tokenManager() {
        return new TokenManagerImpl(this.serverSecret, this.tokenTimeout);
    }

    @Bean
    public VerificationIf verificationImpl() {
        return new VerificationImpl(emailTimeout);
    }

    @Bean
    public FilterRegistrationBean<AuthorizationMiddleware> authorizationMiddleware(TokenManagerIf tokenManager) {
        FilterRegistrationBean<AuthorizationMiddleware> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthorizationMiddleware(tokenManager));
        registrationBean.addUrlPatterns("/v1/protected", "/v1/verifyUser");
        return registrationBean;
    }
}
