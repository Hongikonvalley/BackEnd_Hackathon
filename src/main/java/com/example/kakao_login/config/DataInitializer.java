package com.example.kakao_login.config;

import com.example.kakao_login.entity.*;
import com.example.kakao_login.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

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
    private final MenuItemRepository menuItemRepository;

    @Bean
    @Transactional
    CommandLineRunner initData() {
        return args -> {
            // 사용자 데이터 초기화
            initUser();
            
            // 가비애 매장 데이터 초기화
            initGabiaeStore();
            
            // 가비애 메뉴 데이터 초기화
            initGabiaeMenus();
            
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
        // 기존 가비애 매장 찾기
        storeRepository.findByName("가비애").ifPresentOrElse(
            existingStore -> {
                // 기존 매장이 있으면 외부 링크 정보만 업데이트
                existingStore.setKakaoPlaceId("20809319");
                existingStore.setNaverPlaceId("1071920016");
                storeRepository.save(existingStore);
            },
            () -> {
                // 매장이 없으면 새로 생성
                Store store = Store.builder()
                        .userId("user-001")
                        .name("가비애")
                        .address("서울특별시 와우산로 147-1")
                        .latitude(BigDecimal.valueOf(37.5544229)) // 구글 지도에서 추출한 정확한 좌표
                        .longitude(BigDecimal.valueOf(126.9295616))
                        .phone("070-773-4007")
                        .businessStatus(BusinessStatus.OPEN_24H) // 24시간 영업
                        .aiRecommendation("24시간 영업하는 편리한 카페! 오전 시간 커피 무료 사이즈업 혜택을 놓치지 마세요.")
                        .repImageUrl("https://github.com/user-attachments/assets/0bd68d92-7695-4dc6-aada-d518e369fcb1") // 대표 이미지
                        .kakaoPlaceId("20809319")
                        .naverPlaceId("1071920016")
                        .build();
                storeRepository.save(store);
            }
        );
    }

    private void initGabiaeReviews() {
        // 가비애 매장 찾기
        Store gabiaeStore = storeRepository.findByName("가비애").orElse(null);
        if (gabiaeStore == null) {
            return; // 매장이 없으면 리뷰도 생성하지 않음
        }
        
        String storeId = gabiaeStore.getId();
        
        // 이미 리뷰가 있는지 확인
        long existingReviewCount = storeReviewRepository.countByStoreId(storeId);
        if (existingReviewCount > 0) {
            return; // 이미 리뷰가 있으면 생성하지 않음
        }
        
        // 리뷰 데이터 생성
        List<StoreReview> reviews = Arrays.asList(
                StoreReview.builder()
                        .storeId(storeId)
                        .userId("user-잉뉴")
                        .userNickname("잉뉴")
                        .rating(BigDecimal.valueOf(5.0))
                        .content("홍대생이면 가비애 다 가봤을듯 아주 애용해요. 6시에 가면 아주 쾌적하고 좋다쿽..")
                        .build(),
                StoreReview.builder()
                        .storeId(storeId)
                        .userId("user-펭현숙퀸카")
                        .userNickname("펭현숙퀸카")
                        .rating(BigDecimal.valueOf(5.0))
                        .content("커피 종류 고를 수 있어서 좋고 아침 사이즈업 꿀임")
                        .build(),
                StoreReview.builder()
                        .storeId(storeId)
                        .userId("user-레이싱카")
                        .userNickname("레이싱카")
                        .rating(BigDecimal.valueOf(4.0))
                        .content("2층 자리에 콘센트 많아서 좋습니다!!! 아메리카노 추천 오전 러닝 뛰고 오기 딱 좋아요")
                        .build()
        );
        
        List<StoreReview> savedReviews = storeReviewRepository.saveAll(reviews);
        
        // 리뷰 이미지 데이터 생성 (실제 이미지 URL 사용)
        List<ReviewImage> images = Arrays.asList(
                ReviewImage.builder()
                        .reviewId(savedReviews.get(0).getId()) // 잉뉴 리뷰
                        .imageUrl("https://github.com/user-attachments/assets/6d8a9ce4-6ddd-48fb-b49d-7da679d1b7f4")
                        .sortOrder(1)
                        .build(),
                ReviewImage.builder()
                        .reviewId(savedReviews.get(0).getId()) // 잉뉴 리뷰
                        .imageUrl("https://github.com/user-attachments/assets/72d30657-1470-4695-bc4c-66d63400b785")
                        .sortOrder(2)
                        .build(),
                ReviewImage.builder()
                        .reviewId(savedReviews.get(1).getId()) // 펭현숙퀸카 리뷰
                        .imageUrl("https://github.com/user-attachments/assets/9f0ca3b8-9257-4de3-8c67-bc2cfdbb783f")
                        .sortOrder(1)
                        .build(),
                ReviewImage.builder()
                        .reviewId(savedReviews.get(2).getId()) // 레이싱카 리뷰
                        .imageUrl("https://github.com/user-attachments/assets/b1e1d9e4-48cc-4997-b367-19b0ecec6fbb")
                        .sortOrder(1)
                        .build()
        );
        
        reviewImageRepository.saveAll(images);
    }

    private void initGabiaeMenus() {
        // 가비애 매장 찾기
        Store gabiaeStore = storeRepository.findByName("가비애").orElse(null);
        if (gabiaeStore == null) {
            return; // 매장이 없으면 메뉴도 생성하지 않음
        }
        
        String storeId = gabiaeStore.getId();
        
        // 이미 메뉴가 있는지 확인
        long existingMenuCount = menuItemRepository.countByStoreId(storeId);
        if (existingMenuCount > 0) {
            return; // 이미 메뉴가 있으면 생성하지 않음
        }
        
        // 메뉴 데이터 생성
        List<MenuItem> menus = Arrays.asList(
                MenuItem.builder()
                        .storeId(storeId)
                        .name("아이스 아메리카노 사이즈업")
                        .price(BigDecimal.valueOf(5000))
                        .sortOrder(1)
                        .build()
        );
        
        menuItemRepository.saveAll(menus);
    }
}
