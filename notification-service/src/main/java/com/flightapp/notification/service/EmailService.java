package com.flightapp.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public Mono<Boolean> sendHtmlEmail(String to, String subject, String htmlBody) {
        return Mono.fromCallable(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlBody, true); // true = isHtml

                mailSender.send(message);

                log.info("Email sent successfully to: {}", to);
                return true;
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", to, e.getMessage());
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
