package com.parkez.settlement.scheduler;

import com.parkez.common.aop.CheckMemberStatus;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@Tag(name = "15. 배치 API", description = "주차공간에 대한 예약 API 입니다.")
@Secured(UserRole.Authority.OWNER)
@CheckMemberStatus
public class ApiController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @GetMapping("api/v1/batch")
    public String SettlementApi() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("runtime", LocalDateTime.now().toString())
                .toJobParameters();
        jobLauncher.run(jobRegistry.getJob("settlementJob"), jobParameters);
        return "success";
    }
}
