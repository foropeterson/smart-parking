package com.parking.com.smart.parking.service.impl;


import com.parking.com.smart.parking.dtos.UserDTO;
import com.parking.com.smart.parking.entities.AppRole;
import com.parking.com.smart.parking.entities.Role;
import com.parking.com.smart.parking.entities.User;
import com.parking.com.smart.parking.repository.RoleRepository;
import com.parking.com.smart.parking.repository.UserRepository;
import com.parking.com.smart.parking.security.jwt.response.MessageResponse;
import com.parking.com.smart.parking.security.jwt.response.UserInfoResponse;
import com.parking.com.smart.parking.security.request.SignupRequest;
import com.parking.com.smart.parking.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder encoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    @Override
    public void updateUserRole(Integer userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        AppRole appRole = AppRole.valueOf(roleName);
        Role role = roleRepository.findByRoleName(appRole)
                .orElseThrow(() -> new RuntimeException("role not found"));
        user.setRole(role);
        userRepository.save(user);

    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("user not found")
        );
        return convertToDto(user);
    }

    @Override
    public MessageResponse registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUserName(signUpRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
        }
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Role role;

        if (strRoles == null || strRoles.isEmpty()) {
            role = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        } else {
            String roleStr = strRoles.iterator().next();
            if (roleStr.equals("admin")) {
                role = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            } else {
                role = roleRepository.findByRoleName(AppRole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            }

            user.setAccountNonLocked(true);
            user.setAccountNonExpired(true);
            user.setCredentialsNonExpired(true);
            user.setEnabled(true);
            user.setCredentialsExpiryDate(LocalDate.now().plusYears(1));
            user.setAccountExpiryDate(LocalDate.now().plusYears(1));
            user.setTwoFactorEnabled(false);
            user.setSignUpMethod("email");
        }
        user.setRole(role);
        userRepository.save(user);
        return new MessageResponse("User registered successfully!");
    }

    @Override
    public UserInfoResponse getUserDetails(UserDetails userDetails) {
        Optional<User> user = userRepository.findByUserName(userDetails.getUsername());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(
                user.get().getUserId(),
                user.get().getUserName(),
                user.get().getEmail(),
                user.get().isAccountNonLocked(),
                user.get().isAccountNonExpired(),
                user.get().isCredentialsNonExpired(),
                user.get().isEnabled(),
                user.get().getCredentialsExpiryDate(),
                user.get().getAccountExpiryDate(),
                user.get().isTwoFactorEnabled(),
                roles
        );

        return response;
    }

    private UserDTO convertToDto(User user) {
        UserDTO userDto = new UserDTO();
        userDto.setUserId(user.getUserId());
        userDto.setUserName(user.getUserName());
        userDto.setEmail(user.getEmail());
        userDto.setAccountNonLocked(user.isAccountNonLocked());
        userDto.setAccountNonExpired(user.isAccountNonExpired());
        userDto.setEnabled(user.isEnabled());
        userDto.setCredentialsExpiryDate(user.getCredentialsExpiryDate());
        userDto.setAccountExpiryDate(user.getAccountExpiryDate());
        userDto.setTwoFactorSecret(user.getTwoFactorSecret());
        userDto.setTwoFactorEnabled(user.isTwoFactorEnabled());
        userDto.setSignUpMethod(user.getSignUpMethod());
        userDto.setCreatedDate(user.getCreatedDate());
        userDto.setUpdatedDate(user.getUpdatedDate());
        userDto.setRole(user.getRole());
        return userDto;
    }
    @Override
    public int countTotalUsers() {
        return (int) userRepository.count();
    }
}
