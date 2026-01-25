package com.epam.finaltask.service;

import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.UserPrincipal;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.impl.CustomOAuth2UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RestOperations restOperations;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        customOAuth2UserService = new CustomOAuth2UserService(userRepository, passwordEncoder);
        customOAuth2UserService.setRestOperations(restOperations);
    }

    private OAuth2UserRequest createRequest(String registrationId) {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(registrationId)
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/" + registrationId)
                .userInfoUri("http://localhost:8080/userinfo")
                .userNameAttributeName("sub")
                .authorizationUri("http://localhost:8080/auth")
                .tokenUri("http://localhost:8080/token")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        return new OAuth2UserRequest(clientRegistration, accessToken);
    }

    private void mockRestOperationsResponse(Map<String, Object> attributes) {
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(attributes, HttpStatus.OK);

        when(restOperations.exchange(any(RequestEntity.class), ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenReturn(responseEntity);
    }

    @Test
    @DisplayName("loadUser: Should register new Google user when email does not exist")
    void loadUser_Google_NewUser_Success() {
        // Arrange
        String email = "newuser@gmail.com";
        String name = "New User";

        OAuth2UserRequest request = createRequest("google");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        attributes.put("name", name);
        attributes.put("email", email);
        attributes.put("given_name", "New");
        attributes.put("family_name", "User");

        mockRestOperationsResponse(attributes);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        // Act
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Assert
        assertThat(result).isNotNull().isInstanceOf(UserPrincipal.class);
        UserPrincipal userPrincipal = (UserPrincipal) result;
        assertThat(userPrincipal.getUser().getEmail()).isEqualTo(email);

        // Verify User creation logic
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.isActive()).isTrue();
        assertThat(savedUser.getUsername()).isEqualTo(email);
    }

    @Test
    @DisplayName("loadUser: Should update existing user details when user exists and provider matches")
    void loadUser_Google_ExistingUser_Update_Success() {
        // Arrange
        String email = "existing@gmail.com";
        String oldName = "Old Name";
        String newName = "Updated Name";

        OAuth2UserRequest request = createRequest("google");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "12345");
        attributes.put("name", newName);
        attributes.put("email", email);
        attributes.put("given_name", "Updated");
        attributes.put("family_name", "Name");

        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .username(oldName)
                .authProvider(AuthProvider.GOOGLE)
                .active(true)
                .build();

        mockRestOperationsResponse(attributes);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Assert
        verify(userRepository).save(existingUser);
        assertThat(((UserPrincipal) result).getUser().getFirstName()).isEqualTo("Updated");
        assertThat(((UserPrincipal) result).getUser().getUsername()).isEqualTo(newName);
    }

    @Test
    @DisplayName("loadUser: Should support GitHub provider and extract username correctly")
    void loadUser_Github_Success() {
        // Arrange
        String login = "githubUser";
        String email = "gh@test.com";

        OAuth2UserRequest request = createRequest("github");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", 98765);
        attributes.put("login", login);
        attributes.put("email", email);
        attributes.put("name", "Github Developer");

        mockRestOperationsResponse(attributes);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.GITHUB);
        assertThat(savedUser.getUsername()).isEqualTo(login);
    }

    @Test
    @DisplayName("loadUser: Should throw OAuth2AuthenticationException when provider does not match")
    void loadUser_WrongProvider_ThrowsException() {
        // Arrange
        String email = "user@gmail.com";
        OAuth2UserRequest request = createRequest("google");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "111");
        attributes.put("email", email);
        attributes.put("name", "Test");

        User existingUser = User.builder()
                .email(email)
                .authProvider(AuthProvider.GITHUB)
                .build();

        mockRestOperationsResponse(attributes);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(request))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("Wrong provider used");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("loadUser: Should throw OAuth2AuthenticationException when email/username not found in attributes")
    void loadUser_MissingEmail_ThrowsException() {
        // Arrange
        OAuth2UserRequest request = createRequest("google");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123");
        attributes.put("name", "No Email User");

        mockRestOperationsResponse(attributes);

        // Act & Assert
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(request))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("Email not found");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("loadUser: Should throw InternalAuthenticationServiceException on generic RuntimeException")
    void loadUser_InternalError_ThrowsWrappedException() {
        // Arrange
        String email = "error@test.com";
        OAuth2UserRequest request = createRequest("google");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123");
        attributes.put("email", email);
        attributes.put("name", "Error User");

        mockRestOperationsResponse(attributes);

        when(userRepository.findUserByEmail(email)).thenThrow(new RuntimeException("Database down"));

        // Act & Assert
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(request))
                .isInstanceOf(InternalAuthenticationServiceException.class)
                .hasMessage("Failed to process OAuth2User")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("loadUser: Should throw OAuth2AuthenticationException when factory fails (Unsupported Provider)")
    void loadUser_UnsupportedProvider_ThrowsException() {
        // Arrange
        OAuth2UserRequest request = createRequest("twitter");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "12345");
        attributes.put("id", "1");

        mockRestOperationsResponse(attributes);

        // Act & Assert
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(request))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    @DisplayName("loadUser: Should rethrow OAuth2AuthenticationException from Parent Class")
    void loadUser_ParentFailure_RethrowsException() {
        // Arrange
        OAuth2UserRequest request = createRequest("google");

        when(restOperations.exchange(any(RequestEntity.class), ArgumentMatchers.<ParameterizedTypeReference<Map<String, Object>>>any()))
                .thenThrow(new RestClientException("Network Error"));

        // Act & Assert
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(request))
                .isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    @DisplayName("loadUser: Should NOT update firstName if provider returns null (Branch coverage)")
    void loadUser_Update_NullFirstName_PreservesExisting() {
        // Arrange
        String email = "partial_update@gmail.com";
        String oldFirstName = "OldFirst";
        String oldLastName = "OldLast";
        String newLastName = "NewLast";

        OAuth2UserRequest request = createRequest("google");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "99999");
        attributes.put("email", email);
        attributes.put("name", "Some Name");
        attributes.put("given_name", null);
        attributes.put("family_name", newLastName);

        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .authProvider(AuthProvider.GOOGLE)
                .firstName(oldFirstName)
                .lastName(oldLastName)
                .active(true)
                .build();

        mockRestOperationsResponse(attributes);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Assert
        User userResult = ((UserPrincipal) result).getUser();

        assertThat(userResult.getFirstName()).isEqualTo(oldFirstName);
        assertThat(userResult.getLastName()).isEqualTo(newLastName);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("loadUser: Should NOT update lastName if provider returns null (Branch coverage)")
    void loadUser_Update_NullLastName_PreservesExisting() {
        // Arrange
        String email = "partial_update_last@gmail.com";
        String oldFirstName = "OldFirst";
        String oldLastName = "OldLast";
        String newFirstName = "NewFirst";

        OAuth2UserRequest request = createRequest("google");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "88888");
        attributes.put("email", email);
        attributes.put("name", "Some Name");
        attributes.put("given_name", newFirstName);
        attributes.put("family_name", null);

        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .authProvider(AuthProvider.GOOGLE)
                .firstName(oldFirstName)
                .lastName(oldLastName)
                .active(true)
                .build();

        mockRestOperationsResponse(attributes);
        when(userRepository.findUserByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        OAuth2User result = customOAuth2UserService.loadUser(request);

        // Assert
        User userResult = ((UserPrincipal) result).getUser();

        assertThat(userResult.getFirstName()).isEqualTo(newFirstName);
        assertThat(userResult.getLastName()).isEqualTo(oldLastName);
        verify(userRepository).save(existingUser);
    }
}
