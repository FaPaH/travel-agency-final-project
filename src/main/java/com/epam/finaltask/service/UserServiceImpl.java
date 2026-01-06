package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.AlreadyInUseException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	public UserDTO register(UserDTO userDTO) {
		if (userRepository.existsByUsername(userDTO.getUsername())) {
			throw new AlreadyInUseException("Username is already registered");
		} else if (userRepository.existsByEmail(userDTO.getEmail())) {
			throw new AlreadyInUseException("Email is already registered");
		}

		User user = userMapper.toUser(userDTO);
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		return userMapper.toUserDTO(userRepository.save(user));
	}

	@Override
	public UserDTO updateUser(String username, UserDTO userDTO) {
		if (!userRepository.existsByUsername(userDTO.getUsername())) {
			throw new EntityNotFoundException("User not found");
		} else if (userRepository.existsByEmail(userDTO.getEmail())) {
			throw new AlreadyInUseException("Email is already in use");
		}

		User user = userMapper.toUser(userDTO);

		return userMapper.toUserDTO(userRepository.save(user));
	}

	@Override
	public UserDTO updateUserPassword(String newPassword, UserDTO userDTO) {
		User user = userMapper.toUser(userDTO);

		user.setPassword(passwordEncoder.encode(newPassword));

		return userMapper.toUserDTO(userRepository.save(user));
	}


	@Override
	public UserDTO getUserByUsername(String username) {
		return userMapper.toUserDTO(userRepository.findUserByUsername(username).orElseThrow(
				() -> new EntityNotFoundException("User not found")
		));
	}

	@Override
	public UserDTO changeAccountStatus(UserDTO userDTO) {
		if (!userRepository.existsByUsername(userDTO.getUsername())) {
			throw new EntityNotFoundException("User not found");
		}

		User user = userMapper.toUser(userDTO);
		user.setActive(!user.isActive());

		return userMapper.toUserDTO(userRepository.save(user));
	}

	@Override
	public UserDTO getUserById(UUID id) {
		return userMapper.toUserDTO(userRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException("User not found")
		));
	}

	@Override
	public UserDetailsService userDetailsService() {
		return this::getByUsername;
	}

	@Override
	public User getCurrentUser() {
		var username = SecurityContextHolder.getContext().getAuthentication().getName();
		return userMapper.toUser(getUserByUsername(username));
	}

	@Override
	public UserDTO getUserByEmail(String email) {
		return userMapper.toUserDTO(userRepository.findUserByEmail(email).orElseThrow(
				() -> new EntityNotFoundException("User not found")
		));
	}

	private User getByUsername(String username) {
		return userRepository.findUserByUsername(username).orElseThrow(
				() -> new EntityNotFoundException("User not found")
		);
	}
}