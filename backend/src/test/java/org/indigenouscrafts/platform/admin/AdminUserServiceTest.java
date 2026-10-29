package org.indigenouscrafts.platform.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.indigenouscrafts.platform.auth.dto.UserResponse;
import org.indigenouscrafts.platform.user.AppUser;
import org.indigenouscrafts.platform.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

/**
 * AdminUserServiceTest
 */
@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void listUsers_returnsAllUsersAsResponses() {
        AppUser user1 = AppUser.local("seda", "seda@example.com", "hash1");
        AppUser user2 = AppUser.local("ayse", "ayse@example.com", "hash1");
        when(userRepository.findAll(Sort.by("id"))).thenReturn(List.of(user1, user2));
        List<UserResponse> result = adminUserService.listUsers();

        assertEquals(2, result.size());
        assertEquals("seda", result.get(0).username());
        assertEquals("ayse", result.get(1).username());

        verify(userRepository, never()).save(any());
    }

}