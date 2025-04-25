package com.parkez.alarm.service.templete;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private static final String TEMPLATE_BASE_PATH = "templates/";

    public String generateEmailContent(String templateName, Map<String, String> data) {
        String templatePath = TEMPLATE_BASE_PATH + templateName;

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(templatePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             StringWriter writer = new StringWriter()) {

            String line;
            while ((line = reader.readLine()) != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    line = line.replace("${" + entry.getKey() + "}", entry.getValue());
                }
                writer.write(line + System.lineSeparator());
            }

            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException("이메일 템플릿 로드 또는 파싱 실패: " + templateName, e);
        }
    }
}
