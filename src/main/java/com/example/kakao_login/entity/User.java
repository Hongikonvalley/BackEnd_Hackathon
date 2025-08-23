package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // 기존 테이블명이 다르면 맞춰주세요
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // 로그인 아이디로 사용할 이메일
    private String email;

    @Column(nullable = false) // BCrypt 해시 저장
    private String password;

    private String nickname; // 선택

    private String profileImageUrl;
}
