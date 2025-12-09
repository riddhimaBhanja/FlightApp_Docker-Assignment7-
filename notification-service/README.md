# Notification Service

A reactive microservice for sending email notifications using SMTP with customizable Thymeleaf templates.

## Features

- Send emails with HTML templates using Thymeleaf
- MongoDB storage for notification logs and customer data
- Reactive programming with Spring WebFlux
- SMTP email integration
- RESTful API endpoints
- Customer ID-based notification tracking

## API Endpoints

### 1. Send Notification
```bash
POST /api/notifications/send
Content-Type: application/json

{
  "customerId": "CUST123",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "templateName": "booking-confirmation",
  "subject": "Booking Confirmation",
  "templateData": {
    "pnr": "PNR12345678",
    "flightNumber": "AI101",
    "numberOfSeats": 2,
    "totalAmount": 10000,
    "bookingDate": "2025-12-07"
  }
}
```

### 2. Get Notifications by Customer ID
```bash
GET /api/notifications/customer/{customerId}
```

### 3. Get Notification by ID
```bash
GET /api/notifications/{id}
```

### 4. Get All Notifications
```bash
GET /api/notifications/all
```

## Available Email Templates

### 1. Booking Confirmation (`booking-confirmation`)
Variables:
- `customerName` - Customer's name
- `pnr` - PNR number
- `flightNumber` - Flight number
- `numberOfSeats` - Number of seats booked
- `totalAmount` - Total booking amount
- `bookingDate` - Date of booking

### 2. Welcome Email (`welcome`)
Variables:
- `customerName` - Customer's name
- `customerId` - Customer ID

## Configuration

### SMTP Settings
Configure in `application.properties` or via environment variables:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

For Gmail, you need to:
1. Enable 2-factor authentication
2. Generate an App Password
3. Use the App Password as MAIL_PASSWORD

### MongoDB Connection
```properties
spring.data.mongodb.host=mongodb
spring.data.mongodb.port=27017
spring.data.mongodb.database=notification_db
```

## Docker Deployment

The service is configured in `docker-compose.yml`:

```yaml
notification-service:
  build:
    context: .
    dockerfile: ./notification-service/Dockerfile
  ports:
    - "8083:8083"
  environment:
    MAIL_USERNAME: your-email@gmail.com
    MAIL_PASSWORD: your-app-password
```

Set environment variables before starting:
```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
docker-compose up -d notification-service
```

## Example Usage

### Send a Booking Confirmation Email
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST123",
    "customerName": "Alice Smith",
    "customerEmail": "alice.smith@example.com",
    "templateName": "booking-confirmation",
    "subject": "Flight Booking Confirmation - PNR12345678",
    "templateData": {
      "customerName": "Alice Smith",
      "pnr": "PNR12345678",
      "flightNumber": "AI101",
      "numberOfSeats": 2,
      "totalAmount": 10000,
      "bookingDate": "2025-12-07"
    }
  }'
```

### Get Customer Notifications
```bash
curl http://localhost:8080/api/notifications/customer/CUST123
```

## Creating Custom Templates

1. Create a new HTML file in `src/main/resources/templates/`
2. Use Thymeleaf syntax for variables: `<span th:text="${variableName}">Default</span>`
3. Reference the template by filename (without .html extension)

Example:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <p>Dear <span th:text="${customerName}">Customer</span>,</p>
    <p>Your custom message here with <span th:text="${customVariable}">value</span></p>
</body>
</html>
```

## Technology Stack

- Spring Boot 3.2.0
- Spring WebFlux (Reactive)
- Spring Data MongoDB Reactive
- Spring Mail
- Thymeleaf
- Eureka Client
- MongoDB
- Lombok

## Health Check

```bash
curl http://localhost:8083/actuator/health
```

## Logs

Notification logs are stored in MongoDB `notification_logs` collection with the following status values:
- `PENDING` - Email queued for sending
- `SENT` - Email sent successfully
- `FAILED` - Email sending failed
