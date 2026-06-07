package org.acme.auth.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(unique = true, nullable = false)
    public String email;

    public String password;

    public String name;

    public String role = "USER";

    @Column(name = "oauth_provider")
    public String oauthProvider;

    @Column(name = "oauth_id")
    public String oauthId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    public static Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }
}
