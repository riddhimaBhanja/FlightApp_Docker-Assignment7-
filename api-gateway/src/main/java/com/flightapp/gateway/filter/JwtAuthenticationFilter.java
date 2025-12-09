package com.flightapp.gateway.filter;

import com.flightapp.gateway.dto.ValidateTokenRequest;
import com.flightapp.gateway.dto.ValidateTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GatewayFilter {

    private final WebClient.Builder webClientBuilder;
    private static final String AUTH_SERVICE_URL = "http://auth-service";

    public JwtAuthenticationFilter(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (!request.getHeaders().containsKey("Authorization")) {
            log.warn("Missing Authorization header");
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Invalid Authorization header format");
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // Call auth-service to validate token
        return validateTokenWithAuthService(token)
                .flatMap(response -> {
                    if (Boolean.TRUE.equals(response.getValid())) {
                        log.info("Token validated successfully for user: {}", response.getUsername());

                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-User-Name", response.getUsername())
                                .build();

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    } else {
                        log.warn("Token validation failed: {}", response.getMessage());
                        return onError(exchange, HttpStatus.UNAUTHORIZED);
                    }
                })
                .onErrorResume(error -> {
                    log.error("Error validating token with auth-service: {}", error.getMessage());
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                });
    }

    private Mono<ValidateTokenResponse> validateTokenWithAuthService(String token) {
        ValidateTokenRequest request = ValidateTokenRequest.builder()
                .token(token)
                .build();

        return webClientBuilder.build()
                .post()
                .uri(AUTH_SERVICE_URL + "/api/auth/validate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ValidateTokenResponse.class)
                .onErrorReturn(ValidateTokenResponse.builder()
                        .valid(false)
                        .message("Auth service unavailable")
                        .build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
}
