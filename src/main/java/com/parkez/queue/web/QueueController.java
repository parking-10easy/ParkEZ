package com.parkez.queue.web;

import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.queue.dto.response.MyWaitingQueueDetailResponse;
import com.parkez.queue.dto.response.MyWaitingQueueListResponse;
import com.parkez.queue.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/waiting")
@RestController
@RequiredArgsConstructor
@Tag(name = "18. 대기열 API", description = "대기열 API")
public class QueueController {
    private final QueueService queueService;

    @GetMapping("/me")
    @Operation(summary = "나의 전체 대기열 조회", description = "내가 대기 중인 모든 대기열을 조회합니다.")
    public Response<List<MyWaitingQueueListResponse>> getMyWaitingQueues(@AuthenticatedUser @Parameter(hidden = true) AuthUser authUser) {
        List<MyWaitingQueueListResponse> response = queueService.findMyWaitingQueues(authUser);
        return Response.of(response);
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "예약에 대한 나의 대기열 조회", description = "해당 예약에 대한 나의 대기열 정보를 상세 조회합니다.")
    public Response<MyWaitingQueueDetailResponse> getMyQueue(
            @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long reservationId) {
        MyWaitingQueueDetailResponse response = queueService.findMyQueue(authUser, reservationId);
        return Response.of(response);
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "예약에 대한 나의 대기열 취소", description = "해당 예약에 대한 나의 대기열을 취소합니다.")
    public Response<Void> cancelMyQueue(
            @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long reservationId) {
        queueService.cancelMyQueue(authUser, reservationId);
        return Response.empty();
    }

}
