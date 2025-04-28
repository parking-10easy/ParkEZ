package com.parkez.alarm.service.template;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailTemplateService {

    private static final String TEMPLATE_BASE_PATH = "templates/";

    public String generateEmailContent(String templateName, Map<String, String> data) {
        String templatePath = TEMPLATE_BASE_PATH + templateName;

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(templatePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder templateBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                templateBuilder.append(line).append(System.lineSeparator());
            }

            String content = templateBuilder.toString();

            for (Map.Entry<String, String> entry : data.entrySet()) {
                content = content.replace("${" + entry.getKey() + "}",
                        entry.getValue() != null ? entry.getValue() : "");
            }

            return content;

        } catch (Exception e) {
            throw new RuntimeException("이메일 템플릿 로드 또는 파싱 실패: " + templateName, e);
        }
    }
}
