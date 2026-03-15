# accounting-app

Java Spring Boot accounting app with transaction tracking, filtering, and running balance summary.

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
  "category": "Revenue",
  "amount": 1200.00,
  "type": "INCOME"
}
```

Supported `type` values:
- `INCOME`
- `EXPENSE`

### List transactions

```http
GET /api/transactions
```

Optional query params:
- `type` (`INCOME` or `EXPENSE`)
- `category` (case-insensitive exact match)

Example:

```http
GET /api/transactions?type=EXPENSE&category=operations
```

### Get transaction by id

```http
GET /api/transactions/{id}
```

### Delete transaction

```http
DELETE /api/transactions/{id}
```

### Get summary

```http
GET /api/transactions/summary
```

Response shape:

```json
{
  "income": 1200.0,
  "expenses": 400.0,
  "balance": 800.0
}
```

## Test

```bash
mvn test
```
