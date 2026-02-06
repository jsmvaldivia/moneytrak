package dev.juanvaldivia.moneytrak.security;

import tools.jackson.databind.ObjectMapper;
import dev.juanvaldivia.moneytrak.exception.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthEntryPoint.class);

    private final ObjectMapper objectMapper;

    public CustomAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String username = request.getRemoteUser() != null ? request.getRemoteUser() : "anonymous";
        String ip = request.getRemoteAddr();
        String timestamp = ZonedDateTime.now().toString();

        log.warn("Failed authentication attempt: username='{}', ip='{}', timestamp='{}'", username, ip, timestamp);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("WWW-Authenticate", "Basic realm=\"MoneyTrak API\"");

        var errorResponse = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication required. Provide valid credentials.",
                List.of()
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
