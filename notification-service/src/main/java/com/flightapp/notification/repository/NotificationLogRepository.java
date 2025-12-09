package com.flightapp.notification.repository;

import com.flightapp.notification.entity.NotificationLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationLogRepository extends ReactiveMongoRepository<NotificationLog, String> {

    Flux<NotificationLog> findByCustomerId(String customerId);

    Flux<NotificationLog> findByStatus(String status);
}
