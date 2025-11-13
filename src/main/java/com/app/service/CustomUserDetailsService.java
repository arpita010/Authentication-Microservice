package com.app.service;

import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.app.model.User;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Optional<User> maybeUser = userRepository.findByEmail(email);
    User user =
        maybeUser.orElseThrow(
            () -> new UsernameNotFoundException("User not found with this email id:"));
    List<SimpleGrantedAuthority> authorities =
        (user.getRoles() == null
            ? Collections.emptyList()
            : user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                .collect(Collectors.toList()));
    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())
        .password(user.getPassword() == null ? "" : user.getPassword())
        .authorities(authorities)
        .disabled(!user.getEnabled())
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .build();
  }
}
