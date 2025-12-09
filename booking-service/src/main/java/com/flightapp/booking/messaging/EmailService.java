package com.flightapp.booking.messaging;

import com.flightapp.booking.dto.EmailNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RabbitTemplate rabbitTemplate;

    public void sendBookingConfirmation(EmailNotification notification) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    notification
            );
            log.info("Booking confirmation email sent to queue for PNR: {}", notification.getPnr());
        } catch (Exception e) {
            log.error("Failed to send email to queue: {}", e.getMessage());
        }
    }
}
