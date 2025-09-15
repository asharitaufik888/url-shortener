package com.urlshortener;

import com.urlshortener.dto.request.AuthRequest;
import com.urlshortener.dto.response.AuthResponse;
import com.urlshortener.model.User;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtUtil;
import com.urlshortener.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldReturnTokenWhenCredentialsValid() {
        AuthRequest authRequest = new AuthRequest("user", "password");
        User user = new User();
        user.setUsername("user");
        user.setPassword("hashedPassword");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("user")).thenReturn("mockedToken");
        AuthResponse response = authService.login(authRequest);

        assertEquals("mockedToken", response.getToken());
        verify(jwtUtil).generateToken("user");
    }

    @Test
    void loginShouldThrowExceptionWhenUserNotFound() {
        AuthRequest authRequest = new AuthRequest("unknown", "pass");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(authRequest));
    }

    @Test
    void loginShouldThrowExceptionWhenPasswordInvalid() {
        AuthRequest authRequest = new AuthRequest("user", "wrongPass");
        User user = new User();
        user.setUsername("user");
        user.setPassword("hashedPass");

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "hashedPass")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(authRequest));
    }

    @Test
    void registerShouldSaveUserWhenUsernameIsAvailable() {
        AuthRequest authRequest = new AuthRequest("newuser", "newpass");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newpass")).thenReturn("encodedPass");

        authService.register(authRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("encodedPass", savedUser.getPassword());
    }

    @Test
    void registerShouldThrowExceptionWhenUsernameExist() {
        AuthRequest authRequest = new AuthRequest("existing", "pass");
        User existingUser = new User();
        existingUser.setUsername("existing");

        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalArgumentException.class, () -> authService.register(authRequest));
    }

}
