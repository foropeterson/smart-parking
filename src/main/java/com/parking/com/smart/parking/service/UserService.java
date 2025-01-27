package com.parking.com.smart.parking.service;

import com.parking.com.smart.parking.dtos.UserDTO;
import com.parking.com.smart.parking.entities.Role;
import com.parking.com.smart.parking.entities.User;
import com.parking.com.smart.parking.security.jwt.response.MessageResponse;
import com.parking.com.smart.parking.security.jwt.response.UserInfoResponse;
import com.parking.com.smart.parking.security.request.SignupRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
    void updateUserRole(Integer userId, String roleName);
    List<User> getAllUsers();
    UserDTO getUserById(Integer id);
    MessageResponse registerUser(SignupRequest signUpRequest);
    UserInfoResponse getUserDetails(UserDetails userDetails);
    List<Role> getAllRoles();
    int countTotalUsers();
}
