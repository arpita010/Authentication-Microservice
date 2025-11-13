package com.app.controller;

import com.app.dto.*;
import com.app.model.RefreshToken;
import com.app.model.User;
import com.app.repository.RefreshTokenRepository;
import com.app.repository.UserRepository;
import com.app.service.AuthenticationService;
import com.app.service.CookieService;
import com.app.service.JwtService;
import com.app.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;
  private final AuthenticationService authService;
  private final CookieService cookieService;
  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    log.info("Request REceived");

    RegisterResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {

    Authentication authentication = authenticate(request);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    if (user.getPassword() == null) {
      throw new BadCredentialsException("Password is required.");
    }
    if (!user.getEnabled()) {
      throw new DisabledException("User is disabled");
    }
    String jti = UUID.randomUUID().toString();
    RefreshToken refreshToken =
        RefreshToken.builder()
            .jti(jti)
            .user(user)
            .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
            .revoked(false)
            .build();
    refreshTokenRepository.save(refreshToken);

    String accessToken = jwtService.generateAccessToken(user);
    String generatedRefreshToken = jwtService.generateRefreshToken(user, jti);

    cookieService.attachRefreshCookie(
        response, generatedRefreshToken, (int) jwtService.getRefreshTtlSeconds().longValue());
    cookieService.addNoStoreHeaders(response);

    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .body(
            TokenResponse.bearerWithUser(
                accessToken,
                generatedRefreshToken,
                900,
                new UserDto(
                    user.getName(),
                    user.getEmail(),
                    user.getEnabled(),
                    user.getImage(),
                    user.getCreatedAt(),
                    user.getUpdatedAt())));
  }

  private Authentication authenticate(LoginRequest request) {
    try {
      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid username or password !!");
    }
  }

  @PostMapping("/refresh")
  @Transactional
  public ResponseEntity<TokenResponse> refresh(
      @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    String token =
        readRefreshTokenFromRequest(refreshTokenRequest, request)
            .orElseThrow(() -> new BadCredentialsException("Refresh token missing"));

    if (!jwtService.isRefreshToken(token)) {
      throw new BadCredentialsException("Invalid token type");
    }

    String jti = jwtService.getJti(token);
    UUID userId = jwtService.getUserId(token);

    RefreshToken stored =
        refreshTokenRepository
            .findByJti(jti)
            .orElseThrow(() -> new BadCredentialsException("Refresh token not recognized"));

    if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
      throw new CredentialsExpiredException("Refresh token expired or revoked");
    }
    if (!stored.getUser().getId().equals(userId)) {
      throw new BadCredentialsException("Token subject mismatch");
    }

    // Rotate
    stored.setRevoked(true);
    String newJti = UUID.randomUUID().toString();
    stored.setReplacedByToken(newJti);
    refreshTokenRepository.save(stored);

    User user = stored.getUser();
    RefreshToken newRefreshToken =
        RefreshToken.builder()
            .jti(newJti)
            .user(user)
            .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
            .revoked(false)
            .build();
    refreshTokenRepository.save(newRefreshToken);

    String newAccess = jwtService.generateAccessToken(user);
    String newRefresh = jwtService.generateRefreshToken(user, newJti);

    cookieService.attachRefreshCookie(
        response, newRefresh, (int) jwtService.getRefreshTtlSeconds().longValue());
    cookieService.addNoStoreHeaders(response);

    return ResponseEntity.ok()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccess)
        .body(TokenResponse.bearer(newAccess, newRefresh, 900));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    readRefreshTokenFromRequest(null, request)
        .ifPresent(
            token -> {
              try {
                if (jwtService.isRefreshToken(token)) {
                  String jti = jwtService.getJti(token);
                  refreshTokenRepository
                      .findByJti(jti)
                      .ifPresent(
                          rt -> {
                            rt.setRevoked(true);
                            refreshTokenRepository.save(rt);
                          });
                }
              } catch (Exception e) {
              }
            });

    cookieService.clearRefreshCookie(response);
    cookieService.addNoStoreHeaders(response);
    SecurityContextHolder.clearContext();
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  private Optional<String> readRefreshTokenFromRequest(
      RefreshTokenRequest body, HttpServletRequest request) {
    // 1) Prefer secure HttpOnly cookie
    if (request.getCookies() != null) {
      Optional<String> fromCookie =
          Arrays.stream(request.getCookies())
              .filter(c -> cookieService.getRefreshCookieName().equals(c.getName()))
              .map(Cookie::getValue)
              .filter(v -> v != null && !v.isBlank())
              .findFirst();
      if (fromCookie.isPresent()) {
        return fromCookie;
      }
    }

    // 2) Body
    if (body != null && body.getRefreshToken() != null && !body.getRefreshToken().isBlank()) {
      return Optional.of(body.getRefreshToken().trim());
    }

    // 3) Custom header
    String refreshHeader = request.getHeader("X-Refresh-Token");
    if (refreshHeader != null && !refreshHeader.isBlank()) {
      return Optional.of(refreshHeader.trim());
    }

    // 4) Authorization: Bearer <token> (only if actually refresh)
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
      String candidate = authHeader.substring(7).trim();
      if (!candidate.isEmpty()) {
        try {
          if (jwtService.isRefreshToken(candidate)) {
            return Optional.of(candidate);
          }
        } catch (Exception ignored) {
        }
      }
    }

    return Optional.empty();
  }

  //  @GetMapping("/me")
  //  public User getCurrentUser(Principal principal) {
  //    return userRepository
  //        .findByEmail(principal.getName())
  //        .orElseThrow(() -> new UsernameNotFoundException("You are not loggedIn"));
  //  }
}
