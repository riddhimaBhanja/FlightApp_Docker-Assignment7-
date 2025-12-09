package com.flightapp.notification.service;

import com.flightapp.notification.dto.NotificationRequest;
import com.flightapp.notification.dto.NotificationResponse;
import com.flightapp.notification.entity.NotificationLog;
import com.flightapp.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailTemplateService emailTemplateService;
    private final EmailService emailService;

    public Mono<NotificationResponse> sendNotification(NotificationRequest request) {
        log.info("Processing notification request for customer: {}", request.getCustomerId());

        return Mono.just(request)
                .flatMap(req -> {
                    // Process template
                    String htmlBody = emailTemplateService.processTemplate(
                            req.getTemplateName(),
                            req.getTemplateData()
                    );

                    // Create notification log
                    NotificationLog log = NotificationLog.builder()
                            .customerId(req.getCustomerId())
                            .customerName(req.getCustomerName())
                            .customerEmail(req.getCustomerEmail())
                            .templateName(req.getTemplateName())
                            .subject(req.getSubject())
                            .body(htmlBody)
                            .status("PENDING")
                            .createdAt(LocalDateTime.now())
                            .build();

                    return notificationLogRepository.save(log);
                })
                .flatMap(savedLog -> {
                    // Send email
                    return emailService.sendHtmlEmail(
                            savedLog.getCustomerEmail(),
                            savedLog.getSubject(),
                            savedLog.getBody()
                    ).flatMap(success -> {
                        if (success) {
                            savedLog.setStatus("SENT");
                            savedLog.setSentAt(LocalDateTime.now());
                        } else {
                            savedLog.setStatus("FAILED");
                            savedLog.setErrorMessage("Failed to send email");
                        }
                        return notificationLogRepository.save(savedLog);
                    });
                })
                .map(savedLog -> NotificationResponse.builder()
                        .notificationId(savedLog.getId())
                        .customerId(savedLog.getCustomerId())
                        .customerEmail(savedLog.getCustomerEmail())
                        .status(savedLog.getStatus())
                        .message(savedLog.getStatus().equals("SENT")
                                ? "Email sent successfully"
                                : "Failed to send email")
                        .timestamp(LocalDateTime.now())
                        .build())
                .doOnSuccess(response ->
                        log.info("Notification processed: {} - Status: {}",
                                response.getNotificationId(), response.getStatus()))
                .onErrorResume(e -> {
                    log.error("Error processing notification: {}", e.getMessage());
                    return Mono.just(NotificationResponse.builder()
                            .customerId(request.getCustomerId())
                            .customerEmail(request.getCustomerEmail())
                            .status("FAILED")
                            .message("Error: " + e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
                });
    }

    public Flux<NotificationLog> getNotificationsByCustomerId(String customerId) {
        log.info("Fetching notifications for customer: {}", customerId);
        return notificationLogRepository.findByCustomerId(customerId);
    }

    public Mono<NotificationLog> getNotificationById(String id) {
        log.info("Fetching notification by ID: {}", id);
        return notificationLogRepository.findById(id);
    }

    public Flux<NotificationLog> getAllNotifications() {
        log.info("Fetching all notifications");
        return notificationLogRepository.findAll();
    }
}
