# ApexWallet

ApexWallet is a Spring Boot-based core banking and ledger system that simulates wallet management and financial transaction workflows.

The application provides secure user authentication, wallet operations, transaction recording, idempotent requests, concurrency protection, asynchronous transaction event processing with Apache Kafka, and transaction reporting.

## Features

- JWT-based authentication and authorization
- BCrypt password hashing
- Role-based access control
- User and wallet management
- Deposit and withdrawal operations
- ACID transaction management
- Pessimistic locking for concurrent wallet transactions
- Idempotent transaction requests
- Transaction ledger with successful and failed transactions
- Apache Kafka transaction events
- Asynchronous notification processing
- Transaction reporting and summaries
- Global exception handling
- OpenAPI / Swagger documentation
- Integration tests for concurrency and idempotency

## Tech Stack

- **Java 21**
- **Spring Boot 4.1.1**
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Apache Kafka
- JWT (JJWT)
- Maven
- JUnit

## Architecture

ApexWallet is implemented as a modular Spring Boot application using a layered architecture.

```text
                    Client
                      |
                      v
              +---------------+
              | REST Controllers|
              +-------+-------+
                      |
                      v
              +---------------+
              |    Services   |
              +-------+-------+
                      |
          +-----------+-----------+
          |                       |
          v                       v
   +-------------+          +-----------+
   | Repositories|          |   Kafka   |
   +------+------+          +-----+-----+
          |                       |
          v                       v
   +-------------+       +------------------+
   | PostgreSQL  |       | Kafka Consumers  |
   +-------------+       +--------+---------+
                                  |
                         +--------+--------+
                         |                 |
                         v                 v
                  Notifications       Reporting
```

### Transaction event flow

Successful wallet transactions publish an application event after the database transaction commits.

```text
Wallet Transaction
       |
       v
Database Transaction
       |
       | commit
       v
Transactional Event Listener
       |
       v
Kafka Producer
       |
       v
wallet-transactions topic
       |
       +----------------------+
       |                      |
       v                      v
Notification Consumer    Reporting Consumer
       |                      |
       v                      v
Notification Service    Transaction Reports
```

## Key Design Decisions

### JWT Authentication

Authentication is stateless and uses JSON Web Tokens.

Passwords are hashed using BCrypt before being stored in the database.

```text
Registration
    |
    v
PasswordEncoder
    |
    v
BCrypt hash
    |
    v
PostgreSQL
```

Authenticated requests use:

```text
Authorization: Bearer <JWT>
```

### Authorization

User-specific resources are protected so an authenticated user can only access resources belonging to that user.

Administrative endpoints are restricted using role-based authorization.

### ACID Transactions

Wallet balance updates and transaction records are performed inside database transactions to maintain consistency.

A successful transaction updates both:

- Wallet balance
- Transaction ledger

within the same database transaction.

### Pessimistic Locking

Wallet rows are pessimistically locked during balance-changing operations.

This prevents concurrent withdrawals from reading the same stale balance and causing the wallet to become overdrawn.

For example:

```text
Initial balance: ₹1,000

Withdrawal A: ₹700
Withdrawal B: ₹500

Only one transaction can acquire the wallet lock at a time.

Result:
₹300 or ₹500 remaining
Never a negative balance.
```

### Idempotency

Deposit and withdrawal requests require an `Idempotency-Key`.

If the same idempotency key is submitted again with the same transaction parameters, the existing transaction result is returned instead of processing the transaction twice.

Using the same key with different transaction parameters is rejected.

The idempotency key is stored with a database uniqueness constraint.

### Apache Kafka

Successful transactions generate transaction events after the database transaction commits.

The event is published to the `wallet-transactions` Kafka topic.

Two consumers process the event independently:

- **Notification Consumer** — generates a simulated transaction notification.
- **Reporting Consumer** — stores transaction information for reporting.

This allows transaction processing and downstream processing to remain decoupled.

### Monetary Values

Financial amounts are represented using `BigDecimal` rather than floating-point types.

Database columns use fixed precision and scale for monetary values.

## API Overview

### Authentication

```text
POST /auth/register
POST /auth/login
```

### User Management

```text
GET    /api/users/{id}
PATCH  /api/users/{id}
DELETE /api/users/{id}

GET /api/users
GET /api/users/email?email=<email>
```

The user-list and email lookup endpoints require administrative authorization.

### Wallet

```text
POST /api/users/{id}/wallet
GET  /api/users/{id}/wallet

POST /api/users/{id}/wallet/deposit
POST /api/users/{id}/wallet/withdraw
```

Deposit and withdrawal requests require:

```text
Idempotency-Key: <unique-key>
```

### Transactions

```text
GET /api/users/{id}/transactions
```

### Reporting

```text
GET /api/users/{id}/transaction-reports
GET /api/users/{id}/transaction-reports/summary
```

### Health

```text
GET /actuator/health
```

## Configuration

Sensitive configuration is supplied through environment variables rather than being committed to the repository.

Required variables:

```text
DB_PASSWORD
JWT_SECRET
```

Optional variables with local-development defaults:

```text
DB_URL
DB_USERNAME
KAFKA_BOOTSTRAP_SERVERS
```

Example:

```text
DB_URL=jdbc:postgresql://localhost:5432/apexwallet
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

Do not commit real credentials or secrets to the repository.

## Running the Application

### Prerequisites

- Java 21+
- PostgreSQL
- Docker (for Kafka)
- Maven Wrapper included in the repository

### 1. Start Kafka

The repository includes a Docker Compose configuration for Kafka.

```bash
docker compose up -d
```

### 2. Configure environment variables

Set the required database and JWT environment variables.

### 3. Start the application

On Windows:

```bash
mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

The application runs using the configured PostgreSQL database and Kafka broker.

## API Documentation

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI documentation is available at:

```text
http://localhost:8080/v3/api-docs
```

## Testing

The project includes tests covering:

- Application context loading
- Concurrent withdrawals
- Idempotent duplicate withdrawals
- Idempotency-key conflicts

Run the tests with:

```bash
mvnw.cmd test
```

on Windows, or:

```bash
./mvnw test
```

on Linux/macOS.

## Project Structure

```text
src/main/java/com/app/apexwallet
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── kafka
├── repository
└── service
```

### Main components

- `AuthService` — registration and authentication
- `JwtService` — JWT generation and validation
- `WalletService` — wallet creation and financial transactions
- `TransactionService` — transaction ledger operations
- `ReportingService` — transaction reporting
- `TransactionEventPublisher` — publishes committed transaction events
- `TransactionProducer` — sends events to Kafka
- `NotificationConsumer` — processes transaction notifications
- `ReportingConsumer` — processes transaction reporting events

## Project Goals

ApexWallet was built to explore backend engineering concepts relevant to financial systems, including:

- Secure authentication and authorization
- Database transaction boundaries
- Concurrency control
- Idempotent APIs
- Financial data consistency
- Event-driven processing
- Persistent transaction ledgers
- Asynchronous processing with Kafka
- Backend testing