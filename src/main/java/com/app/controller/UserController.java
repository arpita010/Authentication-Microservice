package com.app.controller;

import com.app.model.User;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

  private final UserRepository userRepository;

  @GetMapping("/currentUser")
  public User getCurrentUser(Principal principal) {
    return userRepository
        .findByEmail(principal.getName())
        .orElseThrow(() -> new UsernameNotFoundException("You are not loggedIn"));
  }
}
