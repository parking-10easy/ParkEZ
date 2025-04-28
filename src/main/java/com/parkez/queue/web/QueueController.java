package com.parkez.queue.web;

import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.queue.dto.response.MyWaitingQueueDetailResponse;
import com.parkez.queue.dto.response.MyWaitingQueueListResponse;
import com.parkez.queue.service.QueueService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
@Tag(name = "16. 대기열 API", description = "대기열 API")
public class QueueController {
    private final QueueService queueService;

    @GetMapping("/me")
    public Response<List<MyWaitingQueueListResponse>> getMyWaitingQueues(@AuthenticatedUser @Parameter(hidden = true) AuthUser authUser) {
        List<MyWaitingQueueListResponse> response = queueService.findMyWaitingQueues(authUser);
        return Response.of(response);
    }

    @GetMapping("/{reservationId}")
    public Response<MyWaitingQueueDetailResponse> getMyQueue(
            @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long reservationId) {
        MyWaitingQueueDetailResponse response = queueService.findMyQueue(authUser, reservationId);
        return Response.of(response);
    }

    @DeleteMapping("/{reservationId}")
    public Response<Void> cancelMyQueue(
            @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long reservationId) {
        queueService.cancelMyQueue(authUser, reservationId);
        return Response.empty();
    }

}
