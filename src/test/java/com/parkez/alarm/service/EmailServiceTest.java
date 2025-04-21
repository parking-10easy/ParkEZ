package com.parkez.alarm.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void 이메일전송_성공시_send가_호출된다() throws MessagingException {
        // when
        emailService.sendEmail("to@email.com", "from@email.com", "제목", "본문");

        // then
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }

//    @Test
//    void 이메일전송_실패시_예외처리된다() throws MessagingException {
//        // given
//        when(javaMailSender.createMimeMessage()).thenThrow(new RuntimeException("전송 오류"));
//
//        // when
//        emailService.sendEmail("to@email.com", "from@email.com", "제목", "본문");
//
//        // then
//        verify(javaMailSender, never()).send(any(MimeMessage.class));
//    }
}