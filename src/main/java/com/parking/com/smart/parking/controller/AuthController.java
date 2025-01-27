package com.parking.com.smart.parking.controller;

import com.parking.com.smart.parking.security.jwt.JwtUtils;
import com.parking.com.smart.parking.security.jwt.response.LoginResponse;
import com.parking.com.smart.parking.security.request.LoginRequest;
import com.parking.com.smart.parking.security.request.SignupRequest;
import com.parking.com.smart.parking.service.AuditLogService;
import com.parking.com.smart.parking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name="Authentication",description = "Apis for managing login and signup")
public class AuthController {

    private final JwtUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

    private final UserService userService;
    private final AuditLogService auditLogService;

    public AuthController(JwtUtils jwtUtils, AuthenticationManager authenticationManager, UserService userService, AuditLogService auditLogService) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }
    @Operation(summary = "signin", description = "login to the systen")
    @PostMapping("/public/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), loginRequest.getPassword()));
            auditLogService.logActionWithLogin(loginRequest.getUsername(),"USERS", null, "SUCCESSFUL_LOGIN",
                    "User successfully logged in with username: " + loginRequest.getUsername());

        } catch (AuthenticationException exception) {
            auditLogService.logActionWithLogin(loginRequest.getUsername(),"USERS", null, "FAILED_LOGIN",
                    "Failed login attempt for username: " + loginRequest.getUsername());
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Incorrect credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        LoginResponse response = new LoginResponse(userDetails.getUsername(), roles, jwtToken);
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Create User Account", description = "Api to create user account")
    @PostMapping("/public/signup")
    public ResponseEntity<?> createUser(@Valid @RequestBody SignupRequest signupRequest) {
        return new ResponseEntity<>(userService.registerUser(signupRequest), HttpStatus.OK);
    }
    @Operation(summary = "Get User Details", description = "Api for getting user details")
    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
        return new ResponseEntity<>(userService.getUserDetails(userDetails), HttpStatus.OK);
    }
    @Operation(summary = "Logged in username", description = "Api to get currently logged in user")
    @GetMapping("/username")
    public String currentUserName(@AuthenticationPrincipal UserDetails userDetails) {
        return (userDetails != null) ? userDetails.getUsername() : "";
    }
}
