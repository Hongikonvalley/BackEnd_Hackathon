package com.example.kakao_login.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공통 엔티티 기본 클래스
 * - 생성/수정 시간 및 활성 상태 관리
 * - Spring Data JPA Auditing 사용
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일시

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 수정일시

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive; // 활성상태

    @PrePersist
    public void prePersist() {
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    /**
     * 소프트 삭제 (논리적 삭제)
     */
    public void softDelete() {
        this.isActive = false;
    }

    /**
     * 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 활성 상태 여부 확인
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }
}