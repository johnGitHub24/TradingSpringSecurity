# TradingSpringSecurity — Spring Boot JWT 安全與訂單 API

> 專注 **Spring Security + JWT** 與 **訂單 CRUD**（H2、無 Kafka/Redis）。
> 參考 [`TradingSpringBoot`](../TradingSpringBoot) 安全與分層結構；測試與文件風格對齊 [`APIGatewayMQ`](../APIGatewayMQ)。

## 文件入口

| 文件 | 說明 |
|------|------|
| [規格書.md](規格書.md) | **主規格（權威）**：架構、API、安全、測試 |
| [TradingSpringSecurity-SPEC.md](TradingSpringSecurity-SPEC.md) | EOS 英文入口／摘要 |
| [docs/architecture.md](docs/architecture.md) | 分層與模組（EOS） |
| [docs/codeGraphic.html](docs/codeGraphic.html) | Tab 式架構圖（Filter／JWT／API／套件） |
| [docs/專案引導教學.html](docs/專案引導教學.html) | 互動引導（長文＋流程） |
| [docs/testing.md](docs/testing.md) | 測試摘要／DoD（EOS） |
| [docs/資料庫設計.md](docs/資料庫設計.md) | 表、Entity、H2 |
| [docs/驗證設計.md](docs/驗證設計.md) | JWT、權限矩陣、錯誤碼 |
| [docs/測試與CI.md](docs/測試與CI.md) | Case ID 對照與 Gradle 指令 |
| [CLAUDE.md](CLAUDE.md) | AI／工程薄規則（EOS 0.1.4） |

## 快速開始

### 需求

- JDK 21

### 本地執行

```powershell
. .\scripts\env.ps1
.\gradlew.bat bootRun
```

- Swagger UI：http://localhost:8080/swagger-ui.html
- H2 Console：http://localhost:8080/h2-console（JDBC：`jdbc:h2:mem:tradingdb`）

### 測試

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\gradlew.bat check
# 或
.\scripts\check.ps1
```

報告：`build/reports/tests/test/index.html`、`build/reports/tests/integrationTest/index.html`

## API 摘要

| Method | 路徑 | 說明 |
|--------|------|------|
| POST | `/api/auth/register` | 註冊 (201) |
| POST | `/api/auth/login` | 登入取得 JWT (200) |
| POST | `/api/v1/orders` | 建單 (201, USER) |
| GET | `/api/v1/orders` | 列表 |
| GET | `/api/v1/orders/{id}` | 單筆 |
| PATCH | `/api/v1/orders/{id}/cancel` | 取消 |
| DELETE | `/api/v1/orders/{id}` | 刪除 (ADMIN) |

## 技術棧

Java 21 · Spring Boot 3.2 · Spring Security (JWT) · Spring Data JPA · H2 · springdoc-openapi · JUnit 5 · Mockito · Gradle

## Document index (EOS)

| File | Description |
|------|-------------|
| [規格書.md](規格書.md) | **Master spec (authority)** |
| [TradingSpringSecurity-SPEC.md](TradingSpringSecurity-SPEC.md) | EOS English entry |
| [docs/architecture.md](docs/architecture.md) | Architecture |
| [docs/testing.md](docs/testing.md) | Test / DoD |
| [docs/資料庫設計.md](docs/資料庫設計.md) | Database |
| [docs/驗證設計.md](docs/驗證設計.md) | Auth / validation |
| [CLAUDE.md](CLAUDE.md) | Thin AI rules (EOS 0.1.4) |

> Docs standard: EngineeringOS eos-minimal/knowledge/documentation.md
