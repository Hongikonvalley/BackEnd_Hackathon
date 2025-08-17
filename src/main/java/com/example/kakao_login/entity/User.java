package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long kakaoId;

    private String email;

    private String nickname;

    // 연관 관계 (필요시 활성화)
    // @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    // private List<StoreReview> storeReviews;

    // @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    // private List<UserFavorite> userFavorites;

    // @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    // private List<UserCoupon> userCoupons;
}
