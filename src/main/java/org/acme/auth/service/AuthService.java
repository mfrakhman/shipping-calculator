package org.acme.auth.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.acme.auth.dto.LoginRequestDto;
import org.acme.auth.dto.RegisterRequestDto;
import org.acme.auth.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;

@ApplicationScoped
public class AuthService {

    @Transactional
    public User register(RegisterRequestDto dto) {
        if (User.findByEmail(dto.email()).isPresent()) {
            throw new WebApplicationException("Email already registered", Response.Status.CONFLICT);
        }

        User user = new User();
        user.email = dto.email();
        user.password = BCrypt.hashpw(dto.password(), BCrypt.gensalt());
        user.name = dto.name();
        user.role = "USER";
        user.persist();

        return user;
    }

    public String login(LoginRequestDto dto) {
        User user = User.findByEmail(dto.email())
                .orElseThrow(() -> new WebApplicationException("Invalid email or password", Response.Status.UNAUTHORIZED));

        if (user.password == null || !BCrypt.checkpw(dto.password(), user.password)) {
            throw new WebApplicationException("Invalid email or password", Response.Status.UNAUTHORIZED);
        }

        return generateToken(user);
    }

    public String generateToken(User user) {
        return Jwt.issuer("shipping-calculator")
                .subject(user.id.toString())
                .claim("email", user.email)
                .claim("name", user.name)
                .groups(user.role)
                .expiresAt(Instant.now().plusSeconds(86400))
                .sign();
    }
}
