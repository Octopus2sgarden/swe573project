package org.indigenouscrafts.platform.auth.dto;

import org.indigenouscrafts.platform.user.AppUser;
import org.indigenouscrafts.platform.user.UserStatus;

public record UserResponse(Long id, String username, String email, UserStatus status) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus()
        );
    }
}