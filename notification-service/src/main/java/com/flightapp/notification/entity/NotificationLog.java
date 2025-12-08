package com.flightapp.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_logs")
public class NotificationLog {

    @Id
    private String id;

    private String customerId;
    private String customerName;
    private String customerEmail;

    private String templateName;
    private String subject;
    private String body;

    private String status; // PENDING, SENT, FAILED
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
