# accounting-app

Starter Java Spring Boot accounting app with a simple transactions API.

## Prerequisites

- Java 17+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

## API

### Create transaction

```http
POST /api/transactions
Content-Type: application/json

{
  "description": "Client payment",
  "amount": 1200.00,
  "type": "INCOME"
}
```

### List transactions

```http
GET /api/transactions
```

## Test

```bash
mvn test
```
