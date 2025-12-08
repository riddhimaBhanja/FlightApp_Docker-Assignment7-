# Flight Booking Microservices Application using Docker

A containerized microservices-based flight booking system built with Spring Boot WebFlux.
Includes secure API access, service discovery, resilient inter-service communication, and event-driven booking notifications.

---

## Overview

The system consists of independently deployable services communicating through the API Gateway and RabbitMQ, with dynamic service discovery via Eureka and persistent storage using MySQL and MongoDB.

---

## Architecture

### Microservices

| Service             | Port | Purpose                                                   |
| ------------------- | ---- | --------------------------------------------------------- |
| **Eureka Server**   | 8761 | Service registry                                          |
| **API Gateway**     | 8080 | Central entry point with JWT authentication               |
| **Flight Service**  | 8081 | Flight inventory operations (MySQL)                       |
| **Booking Service** | 8082 | Booking creation, PNR generation, notifications (MongoDB) |

### Infrastructure

| Component    | Port         | Purpose                             |
| ------------ | ------------ | ----------------------------------- |
| **MySQL**    | 3306         | Inventory database                  |
| **MongoDB**  | 27017        | Booking records                     |
| **RabbitMQ** | 5672 / 15672 | Messaging for booking notifications |

---

## Project Structure (Improved & Organized)

```
flightapp-docker/
│
├── eureka-server/                     # Service Discovery
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── api-gateway/                       # Gateway + JWT Authentication
│   ├── src/
│   │   └── main/java/com/flightapp/gateway/
│   │       ├── config/                # Route configuration
│   │       ├── filter/                # JWT filter
│   │       └── util/                  # JWT utilities
│   ├── Dockerfile
│   └── pom.xml
│
├── flight-service/                    # Inventory Service (MySQL)
│   ├── src/
│   │   ├── main/java/com/flightapp/flight/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   └── dto/
│   │   └── main/resources/
│   │       ├── schema.sql
│   │       └── data.sql
│   ├── Dockerfile
│   ├── pom.xml
│   └── sonar-project.properties
│
├── booking-service/                   # Booking Operations (MongoDB)
│   ├── src/
│   │   ├── main/java/com/flightapp/booking/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── config/                # Feign client for Flight Service
│   │   │   └── messaging/             # RabbitMQ publisher/consumer
│   ├── Dockerfile
│   ├── pom.xml
│   └── sonar-project.properties
│
├── postman/                           # API test collection
│   └── FlightApp-Postman-Collection.json
│
├── jmeter-tests/                      # Load testing plan
│   └── FlightApp-TestPlan.jmx
│
├── docker-compose.yml                 # Full multi-service deployment
├── run-tests.sh                       # Automated test script (Linux/Mac)
└── run-tests.bat                      # Automated test script (Windows)
```

---

## Prerequisites

* Java 17+
* Maven 3.8+
* Docker & Docker Compose
* Optional: Postman/Newman, JMeter

---

## Running the Application

### Start all components

```bash
docker-compose up --build
```

### Key URLs

| Component        | URL                                              |
| ---------------- | ------------------------------------------------ |
| Eureka Dashboard | [http://localhost:8761](http://localhost:8761)   |
| RabbitMQ Console | [http://localhost:15672](http://localhost:15672) |
| API Gateway      | [http://localhost:8080](http://localhost:8080)   |

---

## Authentication

### Generate JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

Include token:

```
Authorization: Bearer <token>
```

---

## Flight Service APIs

### Search Flights

```bash
curl -X POST http://localhost:8080/api/flights/search \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"origin":"DEL","destination":"BOM","travelDate":"2025-12-15"}'
```

### Add Flight

```bash
curl -X POST http://localhost:8080/api/flights/add \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"flightNumber":"AI999","airline":"Air India","origin":"DEL","destination":"BLR","availableSeats":200,"price":6500}'
```

### Get Flight by ID

```bash
curl -X GET http://localhost:8080/api/flights/inventory/1 \
  -H "Authorization: Bearer <token>"
```

---

## Booking Service APIs

### Create Booking

```bash
curl -X POST http://localhost:8080/api/bookings/book \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"flightId":1,"passengerName":"John Doe","passengerEmail":"john@example.com","numberOfSeats":2}'
```

### Get Booking by PNR

```bash
curl -X GET http://localhost:8080/api/bookings/pnr/<PNR> \
  -H "Authorization: Bearer <token>"
```

### Cancel Booking

```bash
curl -X DELETE http://localhost:8080/api/bookings/cancel/<PNR> \
  -H "Authorization: Bearer <token>"
```

---

## RabbitMQ Notifications

* Booking Service publishes notifications to `email.queue`.
* Logs show simulated email payloads.
* View queue status from the RabbitMQ console: **[http://localhost:15672](http://localhost:15672)**

---

## Testing

### Run All Tests

```bash
./run-tests.sh all
# or
run-tests.bat all
```

### Coverage Reports

```
service-name/target/site/jacoco/index.html
```

### Postman Tests

```bash
newman run postman/FlightApp-Postman-Collection.json
```

### JMeter Load Testing

```bash
jmeter -n -t jmeter-tests/FlightApp-TestPlan.jmx
```


## API Summary

| Service | Method | Endpoint                    | Purpose          |
| ------- | ------ | --------------------------- | ---------------- |
| Auth    | POST   | /api/auth/login             | Generate token   |
| Flight  | POST   | /api/flights/search         | Search flights   |
| Flight  | POST   | /api/flights/add            | Add flight       |
| Flight  | GET    | /api/flights/inventory/{id} | Fetch flight     |
| Booking | POST   | /api/bookings/book          | Create booking   |
| Booking | GET    | /api/bookings/pnr/{pnr}     | Retrieve booking |
| Booking | DELETE | /api/bookings/cancel/{pnr}  | Cancel booking   |

---

## Technology Stack

* Spring Boot WebFlux
* Spring Cloud Gateway
* Eureka Discovery
* MySQL / MongoDB
* RabbitMQ
* Feign Clients
* Resilience4j
* Docker / Docker Compose
* JUnit, Mockito, JaCoCo

---


