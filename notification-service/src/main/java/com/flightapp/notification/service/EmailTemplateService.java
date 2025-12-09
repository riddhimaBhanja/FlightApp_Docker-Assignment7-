package com.flightapp.notification.service;

import com.flightapp.notification.exception.TemplateProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public String processTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process(templateName, context);
            log.info("Template {} processed successfully", templateName);
            return htmlContent;
        } catch (Exception e) {
            log.error("Error processing template {}: {}", templateName, e.getMessage());
            throw new TemplateProcessingException("Failed to process email template: " + templateName, e);
        }
    }
}
