package com.example.kakao_login.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 JSON에서 제외
public class MeResponse {
    private boolean ok;
    private String email;
    private String nickname;
    private String profileImageUrl;
}
