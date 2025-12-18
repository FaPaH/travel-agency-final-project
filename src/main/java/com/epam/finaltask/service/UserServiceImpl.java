package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	@Override
	public UserDTO register(UserDTO userDTO) {
		if (userRepository.existsByUsername(userDTO.getUsername())) {
			throw new RuntimeException("User with this username already exists");
		}
		User user = userMapper.toUser(userDTO);
		User savedUser = userRepository.save(user);
		return userMapper.toUserDTO(savedUser);
	}

	@Override
	public UserDTO updateUser(String username, UserDTO userDTO) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found"));

		user.setPhoneNumber(userDTO.getPhoneNumber());

		User savedUser = userRepository.save(user);
		return userMapper.toUserDTO(savedUser);
	}

	@Override
	public UserDTO getUserByUsername(String username) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found"));
		return userMapper.toUserDTO(user);
	}

	@Override
	public UserDTO changeAccountStatus(UserDTO userDTO) {

		userRepository.findById(UUID.fromString(userDTO.getId()))
				.orElseThrow(() -> new RuntimeException("User not found"));

		User user = userMapper.toUser(userDTO);
		User savedUser = userRepository.save(user);
		return userMapper.toUserDTO(savedUser);
	}

	@Override
	public UserDTO getUserById(UUID id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found"));
		return userMapper.toUserDTO(user);
	}
}