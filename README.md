# TradingSpringSecurity — Spring Boot JWT 安全與訂單 API

> 專注 **Spring Security + JWT** 與 **訂單 CRUD**（H2、無 Kafka/Redis）。
> 參考 [`TradingSpringBoot`](../TradingSpringBoot) 安全與分層結構；測試與文件風格對齊 [`APIGatewayMQ`](../APIGatewayMQ)。

## 文件入口

單一入口：本 README。衝突以主規格為準。

| 文件 | 說明 |
|------|------|
| [規格書.md](規格書.md) | **主規格（權威）** |
| [docs/architecture.md](docs/architecture.md) | 分層與模組 |
| [docs/codeGraphic.html](docs/codeGraphic.html) | 架構圖（非權威） |
| [docs/testing.md](docs/testing.md) | 測試／Case／check |
| [docs/資料庫設計.md](docs/資料庫設計.md) | 資料庫 |
| [docs/驗證設計.md](docs/驗證設計.md) | 驗證／權限 |
| [CLAUDE.md](CLAUDE.md) | AI 薄規則 |
| [scripts/README.md](scripts/README.md) | 驗證／啟動腳本 |

## 快速開始

### 需求

- JDK 21（建議 `C:\Program Files\Java\jdk-21.0.12`）

### 驗證（Gate）

```powershell
.\scripts\check.ps1
```

（腳本會載入 `scripts/env.ps1` 再跑 `gradlew check`＝單元 + 整合。）

### 本地執行

終端：

```powershell
. .\scripts\env.ps1
.\gradlew.bat bootRun
```

IntelliJ：**只開本專案根目錄** → Gradle Sync → 跑 **Gradle 任務 `bootRun`**。  
**不要**點 `*Application.java` 綠箭（Windows 易 0xC0000005）。詳見 [docs/IntelliJ-IDE-啟動設定.md](docs/IntelliJ-IDE-啟動設定.md)。

- Swagger UI：http://localhost:8080/swagger-ui.html
- H2 Console：http://localhost:8080/h2-console（JDBC：`jdbc:h2:mem:tradingdb`）

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
| [CLAUDE.md](CLAUDE.md) | Thin AI rules (EOS 0.1.10) |

> Docs standard: EngineeringOS eos-minimal/knowledge/documentation.md

