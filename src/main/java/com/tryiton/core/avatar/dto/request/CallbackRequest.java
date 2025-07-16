package com.tryiton.core.avatar.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CallbackRequest {
    private String taskId;
    private String status;
    private JsonNode result;
    private String message;
}