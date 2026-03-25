# accounting-app

Java Spring Boot accounting app with transaction tracking, filtering, updates, running balance summaries, a built-in dashboard UI, and an H2-backed database layer.

## Prerequisites

- Java 17+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

Then open: `http://localhost:8080`

The app now uses Spring Data JPA with separate writer and reader datasource properties. Read-heavy dashboard and summary endpoints are backed by database-side aggregate queries to reduce application memory pressure at higher record counts. By default both point to the same in-memory H2 database for local development, but the `app.datasource.writer.*` and `app.datasource.reader.*` settings can be pointed at a primary database and a read replica in deployed environments. You can inspect the local database at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:mem:accountingdb`, username `sa`, and a blank password.

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

### Update transaction

```http
PUT /api/transactions/{id}
Content-Type: application/json

{
  "description": "Updated payment",
  "category": "Consulting",
  "amount": 1400.00,
  "type": "INCOME"
}
```

### List transactions

```http
GET /api/transactions
```

The list endpoint is paginated to handle larger datasets efficiently.

Optional query params:
- `page` (zero-based, default `0`)
- `size` (default `25`, max `200`)
- `type` (`INCOME` or `EXPENSE`)
- `category` (case-insensitive exact match)
- `description` (case-insensitive contains search)

Example:

```http
GET /api/transactions?page=0&size=25&type=EXPENSE&category=operations
```

Response shape:

```json
{
  "items": [],
  "page": 0,
  "size": 25,
  "totalItems": 0,
  "totalPages": 0
}
```

### Get transaction by id

```http
GET /api/transactions/{id}
```

### Delete transaction

```http
DELETE /api/transactions/{id}
```


### Get dashboard payload (used by UI)

The dashboard endpoint returns pre-aggregated totals, newest activity, and top category summaries so the UI stays fast as transaction volume grows.

```http
GET /api/transactions/dashboard
```

Response shape:

```json
{
  "transactionCount": 12,
  "income": 4500.0,
  "expenses": 1700.0,
  "balance": 2800.0,
  "recentTransactions": [
    {
      "id": "...",
      "description": "Hosting",
      "category": "Operations",
      "amount": 200.0,
      "type": "EXPENSE",
      "createdAt": "2026-01-01T10:00:00Z"
    }
  ],
  "topCategories": [
    {
      "category": "Revenue",
      "income": 4500.0,
      "expenses": 0.0,
      "balance": 4500.0
    }
  ]
}
```

### Get overall summary

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

### Get summary by category

```http
GET /api/transactions/summary/by-category
```

Response shape:

```json
[
  {
    "category": "Operations",
    "income": 0.0,
    "expenses": 200.0,
    "balance": -200.0
  },
  {
    "category": "Revenue",
    "income": 1200.0,
    "expenses": 0.0,
    "balance": 1200.0
  }
]
```

## Test

```bash
mvn test
```
