package com.tryiton.core.avatar.controller;

import com.tryiton.core.avatar.dto.request.CallbackRequest;
import com.tryiton.core.common.service.AsyncTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/callbacks")
@RequiredArgsConstructor
public class CallbackController {

    private final AsyncTaskService asyncTaskService;

    /**
     * Python 워커로부터 VTON 작업 결과를 받는 콜백 엔드포인트.
     * @param callbackRequest 작업 결과 데이터
     */
    @PostMapping("/vton")
    public ResponseEntity<Void> handleVtonCallback(@RequestBody CallbackRequest callbackRequest) {
        log.info("콜백 수신: Task ID - {}", callbackRequest.getTaskId());
        if ("SUCCESS".equals(callbackRequest.getStatus())) {
            asyncTaskService.completeTask(callbackRequest.getTaskId(), callbackRequest.getResult());
        } else {
            String errorMessage = "작업 실패: " + callbackRequest.getMessage();
            log.error(errorMessage);
            asyncTaskService.failTask(callbackRequest.getTaskId(), new RuntimeException(errorMessage));
        }
        return ResponseEntity.ok().build();
    }
}