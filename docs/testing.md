# 測試與 CI

> Case ID 以 [`../規格書.md`](../規格書.md) 第 7 章為準。  
> 單元 ↔ 整合**同一 Case ID**（見 EngineeringOS `eos-minimal/knowledge/testing.md`）。

## 測試分層

| 層級 | Tag | Gradle 任務 | 外部依賴 |
|------|-----|-------------|----------|
| 單元 | 無 | `gradlew test` | 無（Mockito／Validator） |
| 整合 | `@Tag("integration")` | `gradlew integrationTest` | H2 |
| 全部 | — | `.\scripts\check.ps1` 或 `gradlew check` | 含上列 |

## Case ID 對照（雙層成對）

| Case ID | 單元 | 整合 | 驗證重點 |
|---------|------|------|----------|
| AUTH-001 | `AuthServiceTest` | `AuthIntegrationTest` | 登入成功 → Bearer token |
| AUTH-002 | `AuthServiceTest` | `AuthIntegrationTest` | 錯誤密碼 → BadCredentials／401 |
| AUTH-003 | `UserServiceTest` | `AuthIntegrationTest` | 重複 username → 例外／409 |
| USER-001 | `AuthServiceTest`／`UserServiceTest` | `AuthIntegrationTest` | 註冊編碼密碼／201 |
| JWT-001 | `JwtTokenProviderTest`／Filter | `AuthIntegrationTest`／`SecurityApiIntegrationTest` | 合法 Token 可解析並通過 API |
| JWT-002 | `JwtTokenProviderTest`／Filter | `SecurityApiIntegrationTest` | 竄改 Token 無效／401 |
| JWT-003 | `JwtTokenProviderTest` | `SecurityApiIntegrationTest` | roles round-trip；ADMIN 刪除 |
| ORDER-001 | `OrderServiceTest`／DTO | `SecurityApiIntegrationTest` | 建單 status=NEW／201 |
| ORDER-002 | `OrderServiceTest` | `SecurityApiIntegrationTest` | 重複 clientOrderId → 409 |
| ORDER-003 | `OrderServiceTest` | `SecurityApiIntegrationTest` | 無訂單 → 例外／404 |
| ORDER-004 | `OrderServiceTest` | `SecurityApiIntegrationTest` | 取消 → CANCELLED |
| ORDER-005 | `OrderServiceTest` | `SecurityApiIntegrationTest` | 分頁列表 |
| SEC-001 | `OrderServiceTest`／DTO／Filter | `SecurityApiIntegrationTest` | 已認證建單 201 |
| SEC-002 | `JwtAuthenticationFilterTest` | `SecurityApiIntegrationTest` | 無 token 401 |
| SEC-003 | `CreateOrderRequestValidationTest` | `SecurityApiIntegrationTest` | 驗證失敗 400 |
| SEC-004 | `OrderServiceTest` | `SecurityApiIntegrationTest` | 冪等重複 409 |
| SEC-005 | `OrderServiceTest` | `SecurityApiIntegrationTest` | 取消 → CANCELLED |
| SEC-006 | `JwtAuthenticationFilterTest` | `SecurityApiIntegrationTest` | 非 ADMIN 刪除 403 |
| SEC-007 | `OrderServiceTest` | `SecurityApiIntegrationTest` | ADMIN 刪除 204 |
| DB-001 | `EntityMappingTest` | `DatabaseSchemaIntegrationTest` | users／user_roles／orders |

同一 Case 可出現在多個測試方法（單元＋整合必須描述同一契約）。

## 測試資料

| 檔案 | 用途 |
|------|------|
| `docs/test-data/auth/AUTH-001-SUCCESS.json` | 登入成功範例 payload |

載入工具：`com.trading.security.support.SecurityTestFixtures`

## 指令

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
.\scripts\check.ps1
.\gradlew.bat test
.\gradlew.bat integrationTest
```

成對掃描（WarnOnly）：

```powershell
& "..\EngineeringOS\eos-minimal\hooks\scan-paired-tests.ps1" -ProjectRoot . -WarnOnly
```

報告：`build/reports/tests/test/index.html`、`build/reports/tests/integrationTest/index.html`

## CI 建議（GitHub Actions）

```yaml
name: ci
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
      - run: ./gradlew check
```
