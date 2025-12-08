package com.flightapp.notification.controller;

import com.flightapp.notification.dto.NotificationRequest;
import com.flightapp.notification.dto.NotificationResponse;
import com.flightapp.notification.entity.NotificationLog;
import com.flightapp.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public Mono<ResponseEntity<NotificationResponse>> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        return notificationService.sendNotification(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(NotificationResponse.builder()
                                        .status("FAILED")
                                        .message("Error: " + e.getMessage())
                                        .build())
                ));
    }

    @GetMapping("/customer/{customerId}")
    public Flux<NotificationLog> getNotificationsByCustomerId(@PathVariable String customerId) {
        return notificationService.getNotificationsByCustomerId(customerId);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<NotificationLog>> getNotificationById(@PathVariable String id) {
        return notificationService.getNotificationById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public Flux<NotificationLog> getAllNotifications() {
        return notificationService.getAllNotifications();
    }
}
