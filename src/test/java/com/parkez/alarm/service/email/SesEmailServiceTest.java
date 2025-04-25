package com.parkez.alarm.service.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesEmailServiceTest {

    @Mock
    private SesClient sesClient;

    @InjectMocks
    private SesEmailService sesEmailService;

    @BeforeEach
    void setUp() {
        injectFromEmail(sesEmailService, "noreply@parkez.click");
    }

    @Test
    void sendEmail_정상호출_검증() {
        // given
        String to = "testuser@example.com";
        String subject = "Test Subject";
        String body = "This is a test email.";

        when(sesClient.sendEmail(any(SendEmailRequest.class)))
                .thenReturn(SendEmailResponse.builder().messageId("test-message-id").build());

        // when
        sesEmailService.sendEmail(to, subject, body);

        // then
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient, times(1)).sendEmail(captor.capture());

        SendEmailRequest request = captor.getValue();

        assertThat(request.source()).isEqualTo("noreply@parkez.click");
        assertThat(request.destination().toAddresses()).contains(to);
        assertThat(request.message().subject().data()).isEqualTo(subject);
        assertThat(request.message().body().html().data()).isEqualTo(body);
    }

    private void injectFromEmail(SesEmailService service, String fromEmail) {
        try {
            var field = SesEmailService.class.getDeclaredField("from");
            field.setAccessible(true);
            field.set(service, fromEmail);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
