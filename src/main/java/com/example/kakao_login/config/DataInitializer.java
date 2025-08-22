package com.example.kakao_login.config;

import com.example.kakao_login.entity.*;
import com.example.kakao_login.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final StoreReviewRepository storeReviewRepository;
    private final ReviewImageRepository reviewImageRepository;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            // 사용자 데이터 초기화
            initUser();
            
            // 가비애 매장 데이터 초기화
            initGabiaeStore();
            
            // 가비애 리뷰 데이터 초기화
            initGabiaeReviews();
        };
    }

    private void initUser() {
        String email = "mutsa@mutsa.shop";
        String rawPw = "mutsa1234!";
        userRepository.findByEmail(email).orElseGet(() -> userRepository.save(
                User.builder()
                        .email(email)
                        .password(encoder.encode(rawPw))
                        .nickname("잉뉴")
                        .build()
        ));
    }

    private void initGabiaeStore() {
        if (storeRepository.findActiveById("gabiae-store-001").isEmpty()) {
            Store store = Store.builder()
                    .id("gabiae-store-001")
                    .userId("user-001")
                    .name("가비애")
                    .address("서울특별시 와우산로 147-1")
                    .latitude(BigDecimal.valueOf(37.5563)) // 홍대입구역 근처 추정 좌표
                    .longitude(BigDecimal.valueOf(126.9237))
                    .businessStatus(BusinessStatus.OPEN_24H) // 24시간 영업
                    .aiRecommendation("24시간 영업하는 편리한 카페! 오전 시간 커피 무료 사이즈업 혜택을 놓치지 마세요.")
                    .build();
            storeRepository.save(store);
        }
    }

    private void initGabiaeReviews() {
        String storeId = "gabiae-store-001";
        
        // 리뷰 데이터 생성
        List<StoreReview> reviews = Arrays.asList(
                StoreReview.builder()
                        .id("gabiae-review-001")
                        .storeId(storeId)
                        .userId("user-잉뉴")
                        .userNickname("잉뉴")
                        .rating(BigDecimal.valueOf(5.0))
                        .content("홍대생이면 가비애 다 가봤을듯 아주 애용해요. 6시에 가면 아주 쾌적하고 좋다쿽..")
                        .build(),
                StoreReview.builder()
                        .id("gabiae-review-002")
                        .storeId(storeId)
                        .userId("user-펭현숙퀸카")
                        .userNickname("펭현숙퀸카")
                        .rating(BigDecimal.valueOf(5.0))
                        .content("커피 종류 고를 수 있어서 좋고 아침 사이즈업 꿀임")
                        .build(),
                StoreReview.builder()
                        .id("gabiae-review-003")
                        .storeId(storeId)
                        .userId("user-레이싱카")
                        .userNickname("레이싱카")
                        .rating(BigDecimal.valueOf(4.0))
                        .content("2층 자리에 콘센트 많아서 좋습니다!!! 아메리카노 추천 오전 러닝 뛰고 오기 딱 좋아요")
                        .build()
        );
        
        storeReviewRepository.saveAll(reviews);
        
        // 리뷰 이미지 데이터 생성 (실제 이미지가 없으므로 예시 URL 사용)
        List<ReviewImage> images = Arrays.asList(
                ReviewImage.builder()
                        .id("gabiae-image-001")
                        .reviewId("gabiae-review-001")
                        .imageUrl("https://example.com/gabiae-interior-1.jpg")
                        .sortOrder(1)
                        .build(),
                ReviewImage.builder()
                        .id("gabiae-image-002")
                        .reviewId("gabiae-review-001")
                        .imageUrl("https://example.com/gabiae-coffee-1.jpg")
                        .sortOrder(2)
                        .build(),
                ReviewImage.builder()
                        .id("gabiae-image-003")
                        .reviewId("gabiae-review-002")
                        .imageUrl("https://example.com/gabiae-sizeup-1.jpg")
                        .sortOrder(1)
                        .build(),
                ReviewImage.builder()
                        .id("gabiae-image-004")
                        .reviewId("gabiae-review-003")
                        .imageUrl("https://example.com/gabiae-2nd-floor-1.jpg")
                        .sortOrder(1)
                        .build()
        );
        
        reviewImageRepository.saveAll(images);
    }
}
