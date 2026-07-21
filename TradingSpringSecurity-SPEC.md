# TradingSpringSecurity Specification

> **EOS 入口規格（英文摘要）。** 領域細節以 [規格書.md](規格書.md) 為準。  
> Docs standard: EngineeringOS eos-minimal @ 0.1.4 — `knowledge/documentation.md`

## 0. Document map

| File | Role |
|------|------|
| [規格書.md](規格書.md) | **主規格書（權威）** |
| This file | EOS 英文入口／摘要 |
| [docs/architecture.md](docs/architecture.md) | 分層／模組 |
| [docs/testing.md](docs/testing.md) | 測試／DoD |
| [docs/資料庫設計.md](docs/資料庫設計.md) | users／orders |
| [docs/驗證設計.md](docs/驗證設計.md) | JWT、權限矩陣、錯誤碼 |
| [docs/測試與CI.md](docs/測試與CI.md) | Case ID |
| [CLAUDE.md](CLAUDE.md) | AI 薄規則 |
| [README.md](README.md) | 快速開始 |

## 1. Scope

- **Purpose:** 專注 Spring Security + JWT 與訂單 CRUD（H2）；對照 TradingSpringBoot 但無 Kafka／Redis。
- **Stack:** Java 21 · Spring Boot 3.2 · Spring Security(JWT) · JPA · H2 · springdoc-openapi · JUnit 5
- **Non-goals:** Kafka、Redis、真實撮合、風控規則鏈

## 2. Architecture

`controller` → `service` → `repository`；`security` JWT Filter + `SecurityConfig`。  
見 [docs/architecture.md](docs/architecture.md)。

## 3. API / Contract

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/auth/register`、`/login` | 註冊 201；登入 200 + JWT |
| POST | `/api/v1/orders` | 201；authenticated USER／ADMIN |
| GET | `/api/v1/orders`、`/api/v1/orders/{id}` | 分頁／單筆 |
| PATCH | `/api/v1/orders/{id}/cancel` | → CANCELLED |
| DELETE | `/api/v1/orders/{id}` | 204；**ADMIN only** |

## 4. Test DoD

- [ ] `.\scripts\check.ps1`／`.\gradlew.bat check` green
- [ ] 建單 201、無 token 401、驗證 400、冪等 409
- [ ] 非 ADMIN 刪除 403、ADMIN 刪除 204；DB-001 schema

詳見 [docs/testing.md](docs/testing.md)、[docs/測試與CI.md](docs/測試與CI.md)。

## 5. Changelog

| Date | Note |
|------|------|
| 2026-07-10 | EOS SPEC 入口；摘自 規格書／SecurityConfig／Controllers／測試與CI |
