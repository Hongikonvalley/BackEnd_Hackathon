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
    private final EarlybirdDealRepository earlybirdDealRepository;

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
            
            // 가비애 할인 데이터 초기화
            initGabiaeDeals();
            
            // 가비애 리뷰 데이터 초기화
            initGabiaeReviews();
            
            // 그랑주 카페 데이터 초기화
            initGrangeCafe();
            
            // 그랑주 카페 메뉴 데이터 초기화
            initGrangeMenus();
            
            // 그랑주 카페 리뷰 데이터 초기화
            initGrangeReviews();
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
        // 기존 가비애 매장이 있으면 정보 업데이트
        Store existingStore = storeRepository.findByName("가비애").orElse(null);
        if (existingStore != null) {
            // 대표 이미지 URL 업데이트
            existingStore.setRepImageUrl("https://github.com/user-attachments/assets/84d6b4aa-12e9-40de-92e4-f85a512275d6");
            // AI 추천 내용 업데이트
            existingStore.setAiRecommendation("주로 오전 6시에 방문하고 아이스 아메리카노를 추천해요 잉뉴님께서 좋아하시는 케이크와 한 잔 어떠세요?");
            // 영업 시간 업데이트 (24시간 영업)
            existingStore.setOpenTime("00:00");
            existingStore.setCloseTime("23:59");
            storeRepository.save(existingStore);
            return;
        }
        
        // 새로운 가비애 매장 생성 (기존 매장이 없는 경우)
        Store store = Store.builder()
                .userId("user-001")
                .name("가비애")
                .address("서울특별시 와우산로 147-1")
                .latitude(BigDecimal.valueOf(37.5544229)) // 구글 지도에서 추출한 정확한 좌표
                .longitude(BigDecimal.valueOf(126.9295616))
                .phone("070-773-4007")
                .businessStatus(BusinessStatus.OPEN_24H) // 24시간 영업
                .openTime("00:00") // 24시간 영업 시작
                .closeTime("23:59") // 24시간 영업 종료
                .aiRecommendation("주로 오전 6시에 방문하고 아이스 아메리카노를 추천해요 잉뉴님께서 좋아하시는 케이크와 한 잔 어떠세요?")
                .repImageUrl("https://github.com/user-attachments/assets/84d6b4aa-12e9-40de-92e4-f85a512275d6") // 대표 이미지 (첫 번째 이미지)
                .kakaoPlaceId("20809319")
                .naverPlaceId("1071920016")
                .build();
        storeRepository.save(store);
    }

    private void initGabiaeDeals() {
        // 가비애 매장 찾기
        Store gabiaeStore = storeRepository.findByName("가비애").orElse(null);
        if (gabiaeStore == null) {
            return; // 매장이 없으면 할인 정보도 생성하지 않음
        }

        // 기존 할인 정보가 있는지 확인 (주석처리 - 새로운 데이터 적용을 위해)
        // var existingDeals = earlybirdDealRepository.findTopCurrentByStoreId(gabiaeStore.getId(), java.time.LocalDateTime.now());
        // if (existingDeals.isPresent()) {
        //     return; // 이미 할인 정보가 있으면 생성하지 않음
        // }

        // 오전시간 커피 무료 사이즈업 할인 정보 생성
        EarlybirdDeal deal = EarlybirdDeal.builder()
                .storeId(gabiaeStore.getId())
                .title("오전시간 커피 무료 사이즈업")
                .description("오전 6시~10시 방문시 모든 커피 무료 사이즈업!")
                .dealType(EarlybirdDeal.DealType.EARLYBIRD)
                .discountType(EarlybirdDeal.DiscountType.PERCENT)
                .discountValue("100") // 100% 할인 (무료)
                .displayText("오전시간 사이즈업")
                .status(EarlybirdDeal.DealStatus.ACTIVE)
                .timeWindow("06:00-10:00")
                .build();
        
        earlybirdDealRepository.save(deal);
    }

    private void initGabiaeReviews() {
        // 가비애 매장 찾기
        Store gabiaeStore = storeRepository.findByName("가비애").orElse(null);
        if (gabiaeStore == null) {
            return; // 매장이 없으면 리뷰도 생성하지 않음
        }
        
        String storeId = gabiaeStore.getId();
        
        // 이미 리뷰가 있는지 확인 (주석처리 - 새로운 이미지 적용을 위해)
        // long existingReviewCount = storeReviewRepository.countByStoreId(storeId);
        // if (existingReviewCount > 0) {
        //     return; // 이미 리뷰가 있으면 생성하지 않음
        // }
        
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
        
        // 리뷰 이미지 데이터 생성 (가비애 실제 이미지 URL 사용)
        List<ReviewImage> images = Arrays.asList(
                ReviewImage.builder()
                        .reviewId(savedReviews.get(0).getId()) // 잉뉴 리뷰
                        .imageUrl("https://github.com/user-attachments/assets/174ee3e2-b8c3-4fb9-9a7f-e4b04bb16ab9")
                        .sortOrder(1)
                        .build(),
                ReviewImage.builder()
                        .reviewId(savedReviews.get(1).getId()) // 펭현숙퀸카 리뷰
                        .imageUrl("https://github.com/user-attachments/assets/f1d81032-a660-4a70-895a-f417c038cce7")
                        .sortOrder(2)
                        .build(),
                ReviewImage.builder()
                        .reviewId(savedReviews.get(2).getId()) // 레이싱카 리뷰
                        .imageUrl("https://github.com/user-attachments/assets/6e86abc8-4f1b-4a7d-864b-5479cf0e51af")
                        .sortOrder(3)
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
        
        // 이미 메뉴가 있는지 확인 (주석처리 - 새로운 이미지 적용을 위해)
        // long existingMenuCount = menuItemRepository.countByStoreId(storeId);
        // if (existingMenuCount > 0) {
        //     return; // 이미 메뉴가 있으면 생성하지 않음
        // }
        
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

    private void initGrangeCafe() {
        // 기존 그랑주 매장이 있으면 정보 업데이트
        Store existingStore = storeRepository.findByName("그랑주").orElse(null);
        if (existingStore != null) {
            // 영업 시간 업데이트 (09:00 ~ 00:00)
            existingStore.setOpenTime("09:00");
            existingStore.setCloseTime("00:00");
            storeRepository.save(existingStore);
            return;
        }
        
        // 새로운 그랑주 매장 생성
        Store store = Store.builder()
                .userId("user-001")
                .name("그랑주")
                .address("서울 마포구 서교동 355-2")
                .latitude(BigDecimal.valueOf(37.5541563)) // 구글 지도에서 추출한 정확한 좌표
                .longitude(BigDecimal.valueOf(126.9214322))
                .phone("0507-149-3132")
                .businessStatus(BusinessStatus.OPEN) // 일반 영업
                .openTime("09:00") // 9시 오픈
                .closeTime("00:00") // 24시 마감 (다음날 00:00)
                .aiRecommendation("파르페의 달콤한 유혹이 가득한 카페")
                .repImageUrl("https://github.com/user-attachments/assets/66af8542-388d-42a5-a8d7-6f1f761ff05b") // 대표 이미지 (첫 번째 이미지)
                .kakaoPlaceId("1372734736")
                .naverPlaceId("37915747")
                .build();
        storeRepository.save(store);
    }

    private void initGrangeMenus() {
        // 그랑주 카페 찾기
        Store grangeStore = storeRepository.findByName("그랑주").orElse(null);
        if (grangeStore == null) {
            return; // 매장이 없으면 메뉴도 생성하지 않음
        }
        
        String storeId = grangeStore.getId();
        
        // 이미 메뉴가 있는지 확인
        long existingMenuCount = menuItemRepository.countByStoreId(storeId);
        if (existingMenuCount > 0) {
            return; // 이미 메뉴가 있으면 생성하지 않음
        }
        
        // 메뉴 데이터 생성
        List<MenuItem> menus = Arrays.asList(
                MenuItem.builder()
                        .storeId(storeId)
                        .name("아메리카노+베이글 할인")
                        .price(BigDecimal.valueOf(8000))
                        .sortOrder(1)
                        .build()
        );
        
        menuItemRepository.saveAll(menus);
    }

    private void initGrangeReviews() {
        // 그랑주 카페 찾기
        Store grangeStore = storeRepository.findByName("그랑주").orElse(null);
        if (grangeStore == null) {
            return; // 매장이 없으면 리뷰도 생성하지 않음
        }
        
        String storeId = grangeStore.getId();
        
        // 이미 리뷰가 있는지 확인
        long existingReviewCount = storeReviewRepository.countByStoreId(storeId);
        if (existingReviewCount > 0) {
            return; // 이미 리뷰가 있으면 생성하지 않음
        }
        
        // 리뷰 데이터 생성
        List<StoreReview> reviews = Arrays.asList(
                StoreReview.builder()
                        .storeId(storeId)
                        .userId("user-커피러버")
                        .userNickname("커피러버")
                        .rating(BigDecimal.valueOf(5.0))
                        .content("파르페가 정말 맛있어요! 아메리카노와 베이글 세트도 추천합니다.")
                        .build(),
                StoreReview.builder()
                        .storeId(storeId)
                        .userId("user-디저트퀸")
                        .userNickname("디저트퀸")
                        .rating(BigDecimal.valueOf(4.5))
                        .content("홍대 근처에서 찾기 힘든 고급스러운 분위기 카페예요. 파르페 종류가 많아서 좋아요.")
                        .build(),
                StoreReview.builder()
                        .storeId(storeId)
                        .userId("user-브런치맨")
                        .userNickname("브런치맨")
                        .rating(BigDecimal.valueOf(4.0))
                        .content("9시 오픈이라 아침 브런치하기 좋아요. 베이글과 커피 조합이 완벽해요!")
                        .build(),
                StoreReview.builder()
                        .storeId(storeId)
                        .userId("user-카페탐험가")
                        .userNickname("카페탐험가")
                        .rating(BigDecimal.valueOf(4.5))
                        .content("인테리어가 예쁘고 사진 찍기 좋은 카페입니다. 파르페도 예쁘게 나와요!")
                        .build()
        );
        
        List<StoreReview> savedReviews = storeReviewRepository.saveAll(reviews);
        
        // 리뷰 이미지 데이터 생성 (그랑주 실제 이미지 URL 사용)
        List<ReviewImage> images = Arrays.asList(
                ReviewImage.builder()
                        .reviewId(savedReviews.get(0).getId()) // 커피러버 리뷰
                        .imageUrl("https://github.com/user-attachments/assets/d1850091-cf11-4bf9-94a5-998a6746042e")
                        .sortOrder(1)
                        .build(),
                ReviewImage.builder()
                        .reviewId(savedReviews.get(1).getId()) // 디저트퀸 리뷰
                        .imageUrl("https://github.com/user-attachments/assets/17d5a32c-b0e8-444b-97e9-5b3bb5af722b")
                        .sortOrder(2)
                        .build(),
                ReviewImage.builder()
                        .reviewId(savedReviews.get(2).getId()) // 브런치맨 리뷰
                        .imageUrl("https://github.com/user-attachments/assets/ea5db312-4a4a-4acb-bc64-3afc79245526")
                        .sortOrder(3)
                        .build()
        );
        
        reviewImageRepository.saveAll(images);
    }
}
