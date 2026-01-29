package com.epam.finaltask.service;

import com.epam.finaltask.dto.PaginatedResponse;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.AlreadyInUseException;
import com.epam.finaltask.exception.ResourceNotFoundException;
import com.epam.finaltask.mapper.PaginationMapper;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TokenStorageService<UserDTO> userTokenStorageService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("oldPassword");
        user.setBalance(BigDecimal.TEN);
        user.setActive(true);
        user.setAuthProvider(AuthProvider.LOCAL);

        userDTO = new UserDTO();
        userDTO.setId(userId.toString());
        userDTO.setUsername("testUser");
        userDTO.setEmail("test@example.com");
        userDTO.setActive(true);
        userDTO.setBalance(BigDecimal.TEN);
    }


    @Test
    @DisplayName("saveUser: should successfully save user")
    void saveUser_ShouldSaveUser() {
        String rawPassword = "newPassword";

        // Mocks
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(false);
        when(userMapper.toUser(userDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        // Action
        UserDTO result = userService.saveUser(userDTO, rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(rawPassword, user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("saveUser: exception if username is already in use")
    void saveUser_ShouldThrow_WhenUsernameExists() {
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(true);

        assertThrows(AlreadyInUseException.class, () -> userService.saveUser(userDTO, "pass"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveUser: exception if email is already in use")
    void saveUser_ShouldThrow_WhenEmailExists() {
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(true);

        assertThrows(AlreadyInUseException.class, () -> userService.saveUser(userDTO, "pass"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser: successfully updated user (same email)")
    void updateUser_ShouldUpdate_WhenSameEmail() {
        when(userRepository.findUserByUsername(userDTO.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.updateUser(userDTO.getUsername(), userDTO);

        assertNotNull(result);
        verify(userMapper).updateEntityFromDto(userDTO, user);
        verify(userTokenStorageService, times(1)).revoke(userDTO.getId());
        verify(userTokenStorageService, times(1)).revoke(userDTO.getUsername());
    }

    @Test
    @DisplayName("updateUser: successfully updated user (different email)")
    void updateUser_ShouldUpdate_WhenEmailChangedAndUnique() {
        userDTO.setEmail("new@example.com");
        // user.getEmail() == "test@example.com", dto.getEmail() == "new@example.com"

        when(userRepository.findUserByUsername(userDTO.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot(userDTO.getEmail(), user.getId())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        userService.updateUser(userDTO.getUsername(), userDTO);

        verify(userRepository).existsByEmailAndIdNot("new@example.com", userId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateUser: exception if user is unknown")
    void updateUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findUserByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser("unknown", userDTO));
    }

    @Test
    @DisplayName("updateUser: exception if new email is already in use")
    void updateUser_ShouldThrow_WhenEmailTaken() {
        userDTO.setEmail("taken@example.com");

        when(userRepository.findUserByUsername(userDTO.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot(userDTO.getEmail(), user.getId())).thenReturn(true);

        assertThrows(AlreadyInUseException.class, () -> userService.updateUser(userDTO.getUsername(), userDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changeBalance: successful top-up")
    void changeBalance_ShouldAddAmount() {
        BigDecimal amountToAdd = BigDecimal.valueOf(100);
        BigDecimal expectedBalance = user.getBalance().add(amountToAdd);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        userService.changeBalance(userId.toString(), amountToAdd);

        assertEquals(expectedBalance, user.getBalance());
        verify(userTokenStorageService).revoke(userId.toString());
        verify(userTokenStorageService).revoke(user.getUsername());
    }

    @Test
    @DisplayName("changeBalance: exception with negative top-up")
    void changeBalance_ShouldThrow_WhenAmountNegative() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.changeBalance(userId.toString(), BigDecimal.valueOf(-1)));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changeBalance: exception with zero top-up")
    void changeBalance_ShouldThrow_WhenAmountZero() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.changeBalance(userId.toString(), BigDecimal.ZERO));
    }

    @Test
    @DisplayName("changeBalance: exception with null top-up")
    void changeBalance_ShouldThrow_WhenAmountNull() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.changeBalance(userId.toString(), null));
    }

    @Test
    @DisplayName("changeBalance: exception if user is not found")
    void changeBalance_ShouldThrow_WhenUserMissing() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        String idStr = userId.toString();
        BigDecimal amount = BigDecimal.TEN;

        assertThrows(ResourceNotFoundException.class, () -> userService.changeBalance(idStr, amount));
    }

    @Test
    @DisplayName("getUserByUsername: return from cache")
    void getUserByUsername_ShouldReturnFromCache() {
        when(userTokenStorageService.get(userDTO.getUsername())).thenReturn(userDTO);

        UserDTO result = userService.getUserByUsername(userDTO.getUsername());

        assertEquals(userDTO, result);
        verify(userRepository, never()).findUserByUsername(any());
    }

    @Test
    @DisplayName("getUserByUsername: load from DB if cache is empty")
    void getUserByUsername_ShouldLoadFromDb_WhenCacheMiss() {
        when(userTokenStorageService.get(userDTO.getUsername())).thenReturn(null);
        when(userRepository.findUserByUsername(userDTO.getUsername())).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserByUsername(userDTO.getUsername());

        assertEquals(userDTO, result);
        verify(userTokenStorageService).store(userDTO.getId(), userDTO);
        verify(userTokenStorageService).store(userDTO.getUsername(), userDTO);
    }

    @Test
    @DisplayName("getUserByUsername: exception if cache and DB is empty ошибка")
    void getUserByUsername_ShouldThrow_WhenNotFound() {
        when(userTokenStorageService.get("unknown")).thenReturn(null);
        when(userRepository.findUserByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByUsername("unknown"));
    }

    @Test
    @DisplayName("changeAccountStatus: changes status and call updateUser")
    void changeAccountStatus_ShouldToggleAndCallUpdate() {
        // Arrange
        String username = userDTO.getUsername();
        String userId = userDTO.getId();
        boolean initialStatus = true;

        user.setActive(initialStatus);
        userDTO.setActive(!initialStatus);

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.changeAccountStatus(username);

        // Assert
        assertNotEquals(initialStatus, result.isActive());
        assertNotEquals(initialStatus, user.isActive());

        verify(userRepository).save(user);

        verify(userTokenStorageService).revoke(userId);
        verify(userTokenStorageService).revoke(username);
    }

    @Test
    @DisplayName("getUserById: return from cache")
    void getUserById_ShouldReturnFromCache() {
        when(userTokenStorageService.get(userId.toString())).thenReturn(userDTO);

        UserDTO result = userService.getUserById(userId);

        assertEquals(userDTO, result);
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getUserById: load from DB if cache is empty")
    void getUserById_ShouldLoadFromDb() {
        when(userTokenStorageService.get(userId.toString())).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(userId);

        assertEquals(userDTO, result);
        verify(userTokenStorageService).store(userDTO.getId(), userDTO);
    }

    @Test
    @DisplayName("getUserById: exception if user is not found")
    void getUserById_ShouldThrow_WhenNotFound() {
        when(userTokenStorageService.get(userId.toString())).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    @DisplayName("userDetailsService: successfully load UserDetails with username")
    void userDetailsService_ShouldReturnUser() {
        UserDetailsService uds = userService.userDetailsService();

        when(userRepository.findUserByUsername("testUser")).thenReturn(Optional.of(user));

        UserDetails result = uds.loadUserByUsername("testUser");
        assertEquals(user, result);
    }

    @Test
    @DisplayName("userDetailsService: exception if user is not found")
    void userDetailsService_ShouldThrow_WhenUserNotFound() {
        UserDetailsService uds = userService.userDetailsService();
        when(userRepository.findUserByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> uds.loadUserByUsername("unknown"));
    }

    @Test
    @DisplayName("getUserByEmail: successfully find user")
    void getUserByEmail_ShouldReturnUser() {
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserByEmail("test@example.com");

        assertEquals(userDTO, result);
    }

    @Test
    @DisplayName("getUserByEmail: exception if user is not found")
    void getUserByEmail_ShouldThrow() {
        when(userRepository.findUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail("unknown@example.com"));
    }

    @Test
    @DisplayName("changePassword: change password and provider")
    void changePassword_ShouldUpdatePasswordAndProvider() {
        String newPassword = "newPass";
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(true);
        when(userMapper.toUser(userDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword(userDTO, newPassword);

        assertEquals(newPassword, user.getPassword());
        assertEquals(AuthProvider.LOCAL, user.getAuthProvider());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("changePassword: exception if user is not found")
    void changePassword_ShouldThrow_WhenUserDoesNotExist() {
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.changePassword(userDTO, "pass"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllUsers: return paginated response")
    void getAllUsers_ShouldReturnResponse() {
        Pageable pageable = Pageable.unpaged();
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        PaginatedResponse<UserDTO> expectedResponse = new PaginatedResponse<>();

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        try (MockedStatic<PaginationMapper> paginationMapperMock = mockStatic(PaginationMapper.class)) {
            paginationMapperMock.when(() -> PaginationMapper.toPaginatedResponse(any()))
                    .thenReturn(expectedResponse);

            PaginatedResponse<UserDTO> result = userService.getAllUsers(pageable);

            assertEquals(expectedResponse, result);
            verify(userRepository).findAll(pageable);
        }
    }

    @Test
    @DisplayName("updateUser: successfully updated user when current email is null")
    void updateUser_ShouldUpdate_WhenCurrentEmailIsNull() {
        // Arrange
        user.setEmail(null);
        userDTO.setEmail("new-email@example.com");

        when(userRepository.findUserByUsername(userDTO.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.updateUser(userDTO.getUsername(), userDTO);

        // Assert
        assertNotNull(result);
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), any());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateUser: should skip uniqueness check when email in DTO is same as current")
    void updateUser_ShouldSkipCheck_WhenEmailUnchanged() {
        // Arrange
        user.setEmail("test@example.com");
        userDTO.setEmail("test@example.com");

        when(userRepository.findUserByUsername(userDTO.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        // Act
        userService.updateUser(userDTO.getUsername(), userDTO);

        // Assert
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), any());
        verify(userRepository).save(user);
    }
}
