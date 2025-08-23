package com.example.kakao_login.controller;

import com.example.kakao_login.common.ApiResponse;
import com.example.kakao_login.dto.user.UserResponse;
import com.example.kakao_login.entity.User;
import com.example.kakao_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ApiResponse<UserResponse.MeResponse> getMyInfo(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        UserResponse.MeResponse response = UserResponse.MeResponse.builder()
                .userId(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImageUrl())
                .build();

        return ApiResponse.success(response);
    }
}

