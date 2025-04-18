package com.parkez.alarm.config;

import com.parkez.alarm.domain.entity.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
@RequiredArgsConstructor
public class MailConfig {

    private final MailProperties mailProperties;

    @Bean
    public JavaMailSender javaMailSender() {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // SMTP 서버 설정
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());

        // 추가 JavaMail 프로퍼티
        Properties props = mailSender.getJavaMailProperties();
        props.put("spring.mail.transport.protocol", "smtp");
        props.put("spring.mail.smtp.auth", "true");
        props.put("spring.mail.smtp.starttls.enable", "true");
        props.put("spring.mail.debug", "true");
        props.put("spring.mail.smtp.ssl.enable", "true");
        props.put("spring.mail.smtp.ssl.trust", "smtp.test.com");

        return mailSender;
    }
}
