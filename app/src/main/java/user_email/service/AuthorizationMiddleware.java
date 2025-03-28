package user_email.service;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import user_email.api.TokenManagerIf;

public class AuthorizationMiddleware implements Filter {

    private static final Logger logger = LogManager.getLogger(AuthorizationMiddleware.class);

    private final TokenManagerIf tokenManager;

    public AuthorizationMiddleware(TokenManagerIf tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.info("Authorization header not found");
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String token = authorizationHeader.substring(7);
        SimpleEntry<String, String> entry = null;
        try {
            entry = tokenManager.verifyToken(token);
        }catch (TokenExpiredException e) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }catch (SignatureVerificationException e) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }catch (MissingClaimException e) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }catch (Exception e) {
            logger.info("Got exception: ", e);
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        httpServletRequest.setAttribute("username", entry.getKey());
        httpServletRequest.setAttribute("email", entry.getValue());

        chain.doFilter(request, response);
    }
}
