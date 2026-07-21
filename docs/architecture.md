# Architecture — TradingSpringSecurity

> 衝突以 [規格書.md](../規格書.md) 為準。

## Layers

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Controller | `controller` | HTTP、`@Valid`、ResponseEntity |
| Service | `service` | Auth／User／Order 商業邏輯、交易 |
| Security | `security` | JWT 簽發／驗證／Filter |
| Repository | `repository` | JPA |
| Entity / DTO / Domain | `entity`、`dto`、`domain` | 映射與契約 |
| Config | `config` | `SecurityConfig`、OpenAPI |
| Exception | `exception` | `GlobalExceptionHandler` |

## Module map

| Module | Notes |
|--------|-------|
| Auth | `AuthController` — `/api/auth/register`、`/login` |
| Order | `OrderController` — `/api/v1/orders` CRUD／cancel；DELETE 需 ADMIN |
| Security | BCrypt + JWT HS256 + STATELESS session |
| Ops | Actuator health／metrics、Swagger |

**不在範圍：** Kafka、Redis（見 TradingSpringBoot）。

## Runtime

```text
Client (Bearer JWT)
  → JwtAuthenticationFilter
  → AuthController / OrderController
      → AuthService / OrderService / UserService
          → Repository → H2 (jdbc:h2:mem:tradingdb)
```

Port：`8080`。

## Visual maps

| 文件 | 用途 |
|------|------|
| [codeGraphic.html](codeGraphic.html) | Tab：Filter／JWT／API／套件（圖為主） |
| [專案引導教學.html](專案引導教學.html) | 長文引導＋流程圖 |
