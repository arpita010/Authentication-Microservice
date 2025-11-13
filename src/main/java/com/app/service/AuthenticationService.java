package com.app.service;

import com.app.constants.Provider;
import com.app.dto.RegisterRequest;
import com.app.dto.RegisterResponse;
import com.app.model.User;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public RegisterResponse register(RegisterRequest request) {
    if (request == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
    }
    if (request.getEmail() == null || request.getEmail().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
    }
    if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }

    String encoded = null;
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      encoded = passwordEncoder.encode(request.getPassword());
    }

    User user =
        User.builder()
            .email(request.getEmail().trim().toLowerCase())
            .name(request.getName())
            .provider(Provider.LOCAL)
            .password(encoded) // null allowed for OAuth-only users
            .image(request.getImage())
            .enabled(true)
            .build();

    User saved = userRepository.save(user);

    return RegisterResponse.builder()
        .id(saved.getId())
        .email(saved.getEmail())
        .name(saved.getName())
        .image(saved.getImage())
        .enabled(saved.getEnabled())
        .createdAt(saved.getCreatedAt())
        .updatedAt(saved.getUpdatedAt())
        .build();
  }
}
