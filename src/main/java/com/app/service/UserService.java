package com.app.service;

import com.app.constants.Provider;
import com.app.model.User;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User saveUserIfNotExit(
      String providerId, String email, String username, String image, Provider provider) {

    User user =
        userRepository
            .findByEmail(email)
            .orElseGet(
                () ->
                    User.builder()
                        .providerId(providerId)
                        .email(email)
                        .name(username)
                        .provider(provider)
                        .image(image)
                        .password("")
                        .enabled(true)
                        .build());
    return userRepository.save(user);
  }
}
