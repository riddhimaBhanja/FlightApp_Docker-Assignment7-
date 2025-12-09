package com.flightapp.booking.messaging;

import com.flightapp.booking.dto.EmailNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EmailService emailService;

    private EmailNotification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = EmailNotification.builder()
                .to("test@example.com")
                .subject("Booking Confirmation")
                .pnr("PNR12345678")
                .flightNumber("AI101")
                .passengerName("John Doe")
                .body("Your booking is confirmed")
                .build();
    }

    @Test
    void testSendBookingConfirmation_Success() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EmailNotification.class));

        emailService.sendBookingConfirmation(testNotification);

        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                testNotification
        );
    }

    @Test
    void testSendBookingConfirmation_Exception() {
        doThrow(new RuntimeException("RabbitMQ connection failed"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EmailNotification.class));

        emailService.sendBookingConfirmation(testNotification);

        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                testNotification
        );
    }

    @Test
    void testSendBookingConfirmation_NullPnr() {
        EmailNotification notificationWithNullPnr = EmailNotification.builder()
                .to("test@example.com")
                .subject("Booking Confirmation")
                .pnr(null)
                .build();

        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EmailNotification.class));

        emailService.sendBookingConfirmation(notificationWithNullPnr);

        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                notificationWithNullPnr
        );
    }

    @Test
    void testSendBookingConfirmation_MultipleEmails() {
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(EmailNotification.class));

        emailService.sendBookingConfirmation(testNotification);
        emailService.sendBookingConfirmation(testNotification);
        emailService.sendBookingConfirmation(testNotification);

        verify(rabbitTemplate, times(3)).convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                testNotification
        );
    }
}
