package de.iu.raumbuchung.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // tabelle users
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment id
    private Long id;

    @Column(nullable = false, unique = true, length = 50) // eindeutiger username
    private String username;

    @Column(nullable = false, unique = true, length = 100) // eindeutige email
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255) // gehashes passwort
    private String passwordHash;

    @Column(nullable = false, length = 20) // PENDING, ACTIVE, BLOCKED
    private String status;

    @Column(nullable = false, length = 20) // USER oder ADMIN
    private String role;

    // leerer konstruktor für jpa
    public User() {
    }

    // einfacher konstruktor
    public User(String username, String email, String passwordHash, String status, String role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.role = role;
    }

    // getter und setter
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
