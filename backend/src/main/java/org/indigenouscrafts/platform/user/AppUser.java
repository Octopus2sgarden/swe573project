package org.indigenouscrafts.platform.user;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * A registered user of the platform, stored in the {@code app_user} table
 * (created by V2__users.sql).
 *
 * <p>The class is called AppUser, not User, so it is not confused with
 * Spring Security's own {@code User} class that we will use in #28.
 *
 * <p>The password is never stored. Only its BCrypt hash is kept in
 * {@link #passwordHash}; hashing happens in the registration service (#27).
 */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 10)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_subject")
    private String providerSubject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "display_name", length = 60)
    private String displayName;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(name = "profile_picture_key")
    private String profilePictureKey;

    @Column(name = "location_text", length = 100)
    private String locationText;

    @Column(name = "email_notifications", nullable = false)
    private boolean emailNotifications = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Required by JPA. Use {@link #local} to create a new user. */
    protected AppUser() {
    }

    /** Creates a user who signs in with email and password. */
    public static AppUser local(String username, String email, String passwordHash) {
        AppUser user = new AppUser();
        user.username = username;
        user.email = email;
        user.passwordHash = passwordHash;
        user.authProvider = AuthProvider.LOCAL;
        return user;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void activate() {
        status = UserStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public AuthProvider getAuthProvider() { return authProvider; }
    public String getProviderSubject() { return providerSubject; }
    public Role getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public String getDisplayName() { return displayName; }
    public String getBio() { return bio; }
    public String getProfilePictureKey() { return profilePictureKey; }
    public String getLocationText() { return locationText; }
    public boolean isEmailNotifications() { return emailNotifications; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setRole(Role role) { this.role = role; }
    public void setStatus(UserStatus status) { this.status = status; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setBio(String bio) { this.bio = bio; }
    public void setProfilePictureKey(String key) { this.profilePictureKey = key; }
    public void setLocationText(String locationText) { this.locationText = locationText; }
    public void setEmailNotifications(boolean on) { this.emailNotifications = on; }
}
