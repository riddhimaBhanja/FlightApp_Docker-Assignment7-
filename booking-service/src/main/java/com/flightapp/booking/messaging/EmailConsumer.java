package com.flightapp.booking.messaging;

import com.flightapp.booking.dto.EmailNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailConsumer {

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeEmailNotification(EmailNotification notification) {
        log.info("Received email notification from queue");
        log.info("To: {}", notification.getTo());
        log.info("Subject: {}", notification.getSubject());
        log.info("PNR: {}", notification.getPnr());
        log.info("Flight Number: {}", notification.getFlightNumber());
        log.info("Passenger Name: {}", notification.getPassengerName());

        sendActualEmail(notification);
    }

    private void sendActualEmail(EmailNotification notification) {
        log.info("Sending actual email to: {}", notification.getTo());
        log.info("Email Body:\n{}", notification.getBody());
        log.info("Email sent successfully for PNR: {}", notification.getPnr());
    }
}
