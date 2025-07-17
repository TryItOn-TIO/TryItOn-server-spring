package com.tryiton.core.avatar.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CeleryTaskResultDto {
    private String status;
    private JsonNode result; // 다양한 결과 구조를 받기 위해 JsonNode 사용
}