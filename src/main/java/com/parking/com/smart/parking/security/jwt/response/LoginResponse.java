package com.parking.com.smart.parking.security.jwt.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String username;
    private List<String> roles;

    public LoginResponse(String username, List<String> roles, String accessToken) {
        this.username = username;
        this.roles = roles;
        this.accessToken = accessToken;
    }
}
