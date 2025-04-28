package com.parkez.alarm.service.template;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTemplateServiceTest {

    private final EmailTemplateService emailTemplateService = new EmailTemplateService();

    @Test
    void 이메일_템플릿_치환_성공() {
        // given
        String templateName = "payment_notification.html";
        Map<String, String> data = Map.of(
                "userName", "홍길동",
                "reservationId", "12345"
        );

        // when
        String content = emailTemplateService.generateEmailContent(templateName, data);

        // then
        assertThat(content).contains("<strong>홍길동</strong>님");
        assertThat(content).contains("<li><strong>예약 번호 :</strong> 12345</li>");
    }

    @Test
    void 존재하지_않는_템플릿_예외_발생() {
        // given
        String invalidTemplateName = "not_exist_template.html";

        // when & then
        assertThatThrownBy(() -> emailTemplateService.generateEmailContent(invalidTemplateName, Map.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이메일 템플릿 로드 또는 파싱 실패");
    }
}
