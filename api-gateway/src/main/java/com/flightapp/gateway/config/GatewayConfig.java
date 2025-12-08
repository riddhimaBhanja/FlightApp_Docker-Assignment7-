package com.flightapp.gateway.config;

import com.flightapp.gateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private static final String AUTH_SERVICE_URI = "lb://auth-service";
    private static final String FLIGHT_SERVICE_URI = "lb://flight-service";
    private static final String BOOKING_SERVICE_URI = "lb://booking-service";
    private static final String NOTIFICATION_SERVICE_URI = "lb://notification-service";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public GatewayConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Flight Service Routes - All secured with JWT
                .route("flight-search", r -> r
                        .path("/api/flights/search")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(FLIGHT_SERVICE_URI))
                .route("flight-add", r -> r
                        .path("/api/flights/add")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(FLIGHT_SERVICE_URI))
                .route("flight-inventory", r -> r
                        .path("/api/flights/inventory/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(FLIGHT_SERVICE_URI))

                // Booking Service Routes - All secured with JWT
                .route("booking-create", r -> r
                        .path("/api/bookings/book")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(BOOKING_SERVICE_URI))
                .route("booking-cancel", r -> r
                        .path("/api/bookings/cancel/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(BOOKING_SERVICE_URI))
                .route("booking-get", r -> r
                        .path("/api/bookings/pnr/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(BOOKING_SERVICE_URI))

                // Notification Service Routes - Secured with JWT
                .route("notification-send", r -> r
                        .path("/api/notifications/send")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(NOTIFICATION_SERVICE_URI))
                .route("notification-customer", r -> r
                        .path("/api/notifications/customer/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(NOTIFICATION_SERVICE_URI))
                .route("notification-all", r -> r
                        .path("/api/notifications/all")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(NOTIFICATION_SERVICE_URI))
                .route("notification-get", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri(NOTIFICATION_SERVICE_URI))

                // Auth endpoints (for login/register/token generation) - Not secured
                .route("auth-endpoints", r -> r
                        .path("/api/auth/**")
                        .uri(AUTH_SERVICE_URI))

                .build();
    }
}
