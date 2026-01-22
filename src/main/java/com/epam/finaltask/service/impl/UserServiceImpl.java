package com.epam.finaltask.service.impl;

import com.epam.finaltask.dto.PaginatedResponse;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.AlreadyInUseException;
import com.epam.finaltask.exception.ResourceNotFoundException;
import com.epam.finaltask.mapper.PaginationMapper;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.TokenStorageService;
import com.epam.finaltask.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final TokenStorageService<UserDTO> userTokenStorageService;

	@Override
	public UserDTO saveUser(UserDTO userDTO, String password) {
		if (userRepository.existsByUsername(userDTO.getUsername())) {
			throw new AlreadyInUseException("Username", userDTO.getUsername());
		} else if (userRepository.existsByEmail(userDTO.getEmail())) {
			throw new AlreadyInUseException("Email", userDTO.getEmail());
		}

		User user = userMapper.toUser(userDTO);
		user.setPassword(password);

		return userMapper.toUserDTO(userRepository.save(user));
	}

	@Override
	public UserDTO updateUser(String username, UserDTO userDTO) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", username));

		if (user.getEmail() != null && !user.getEmail().equals(userDTO.getEmail())) {
			if (userRepository.existsByEmailAndIdNot(userDTO.getEmail(), user.getId())) {
				throw new AlreadyInUseException("Email", userDTO.getEmail());
			}
		}

		userMapper.updateEntityFromDto(userDTO, user);

		UserDTO returnUser = userMapper.toUserDTO(userRepository.save(user));

		userTokenStorageService.revoke(returnUser.getId());
		userTokenStorageService.revoke(returnUser.getUsername());

		return returnUser;
	}

	@Override
	public UserDTO changeBalance(String userId, BigDecimal amount) {
		User user = userRepository.findById(UUID.fromString(userId))
				.orElseThrow(() -> new ResourceNotFoundException("User", userId));

		if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
			user.setBalance(user.getBalance().add(amount));

			userTokenStorageService.revoke(user.getId().toString());
			userTokenStorageService.revoke(user.getUsername());

			return userMapper.toUserDTO(userRepository.save(user));
		} else {
			throw new RuntimeException("Amount cannot be negative or null");
		}
	}

	@Override
	public UserDTO getUserByUsername(String username) {
		UserDTO user = userTokenStorageService.get(username);

		if (user == null) {
			user = userMapper.toUserDTO(userRepository.findUserByUsername(username).orElseThrow(
					() ->  new ResourceNotFoundException("User", username)
			));
			userTokenStorageService.store(user.getId(), user);
			userTokenStorageService.store(user.getUsername(), user);
		}

		return user;
	}

	@Override
	public UserDTO changeAccountStatus(UserDTO userDTO) {
		userDTO.setActive(!userDTO.isActive());

		UserDTO returnUser = updateUser(userDTO.getUsername(), userDTO);

		userTokenStorageService.revoke(returnUser.getId());
		userTokenStorageService.revoke(returnUser.getUsername());

		return returnUser;
	}

	@Override
	public UserDTO getUserById(UUID id) {
		UserDTO user = userTokenStorageService.get(id.toString());

		if (user == null) {
			user = userMapper.toUserDTO(userRepository.findById(id).orElseThrow(
					() ->  new ResourceNotFoundException("User", id)
			));
			userTokenStorageService.store(user.getId(), user);
			userTokenStorageService.store(user.getUsername(), user);
		}

		return user;
	}

	@Override
	public UserDetailsService userDetailsService() {
		return this::getByUsername;
	}

	@Override
	public UserDTO getUserByEmail(String email) {
		return userMapper.toUserDTO(userRepository.findUserByEmail(email).orElseThrow(
				() ->  new ResourceNotFoundException("User", email)
		));
	}

	@Override
	public void changePassword(UserDTO userDTO, String newPassword) {
		if (!userRepository.existsByUsername(userDTO.getUsername())) {
			throw new ResourceNotFoundException("User", userDTO.getUsername());
		}

		User user = userMapper.toUser(userDTO);
		user.setPassword(newPassword);
		user.setAuthProvider(AuthProvider.LOCAL);

		userMapper.toUserDTO(userRepository.save(user));
	}

	@Override
	public PaginatedResponse<UserDTO> getAllUsers(Pageable pageable) {
		Page<UserDTO> dtoPage = userRepository.findAll(pageable).map(userMapper::toUserDTO);

		return PaginationMapper.toPaginatedResponse(dtoPage);
	}

	private User getByUsername(String username) {
		return userRepository.findUserByUsername(username).orElseThrow(
				() ->  new ResourceNotFoundException("User", username)
		);
	}
}