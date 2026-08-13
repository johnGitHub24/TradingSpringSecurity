# 測試與 CI

> Case ID 以 [`../規格書.md`](../規格書.md) 第 7 章為準。

## 測試分層

| 層級 | Tag | Gradle 任務 | 外部依賴 |
|------|-----|-------------|----------|
| 單元 | 無 | `gradlew test` | 無（Mockito） |
| 整合 | `@Tag("integration")` | `gradlew integrationTest` | H2 |
| 全部 | — | `gradlew check` | 含上列 |

## Case ID 對照

| Case ID | 類型 | 測試類別 | 驗證重點 |
|---------|------|----------|----------|
| JWT-UNIT-001 | 單元 | `JwtTokenProviderTest` | 產生 token、驗證、username |
| JWT-UNIT-002 | 單元 | `JwtTokenProviderTest` | 竄改 token 驗證失敗 |
| JWT-UNIT-003 | 單元 | `JwtTokenProviderTest` | roles round-trip |
| ORDER-UNIT-001 | 單元 | `OrderServiceTest` | 建單 status=NEW |
| ORDER-UNIT-002 | 單元 | `OrderServiceTest` | 重複 clientOrderId 不 save |
| ORDER-UNIT-003 | 單元 | `OrderServiceTest` | 無訂單拋 `OrderNotFoundException` |
| ORDER-UNIT-004 | 單元 | `OrderServiceTest` | 取消 → CANCELLED |
| AUTH-UNIT-001 | 單元 | `AuthServiceTest` | register 委派 UserService |
| AUTH-UNIT-002 | 單元 | `AuthServiceTest` | login 回傳 Bearer token |
| USER-UNIT-001 | 單元 | `UserServiceTest` | 註冊編碼密碼並儲存 |
| USER-UNIT-002 | 單元 | `UserServiceTest` | 重複 username 409 例外 |
| AUTH-001 | 整合 | `AuthIntegrationTest` | 登入成功有 token |
| AUTH-002 | 整合 | `AuthIntegrationTest` | 錯誤密碼 401 |
| AUTH-003 | 整合 | `AuthIntegrationTest` | 重複註冊 409 |
| SEC-001 | 整合 | `SecurityApiIntegrationTest` | 建單 201 |
| SEC-002 | 整合 | `SecurityApiIntegrationTest` | 無 token 401 |
| SEC-003 | 整合 | `SecurityApiIntegrationTest` | 驗證失敗 400 |
| SEC-004 | 整合 | `SecurityApiIntegrationTest` | 冪等鍵重複 409 |
| SEC-005 | 整合 | `SecurityApiIntegrationTest` | 取消 → CANCELLED |
| SEC-006 | 整合 | `SecurityApiIntegrationTest` | 非 ADMIN 刪除 403 |
| SEC-007 | 整合 | `SecurityApiIntegrationTest` | ADMIN 刪除 204 |
| DB-001 | 整合 | `DatabaseSchemaIntegrationTest` | users / user_roles / orders 表存在 |

## 測試資料

| 檔案 | 用途 |
|------|------|
| `docs/test-data/auth/AUTH-001-SUCCESS.json` | 登入成功範例 payload |

載入工具：`com.trading.security.support.SecurityTestFixtures`

## 指令

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
.\gradlew.bat clean check
.\gradlew.bat test
.\gradlew.bat integrationTest
.\scripts\check.ps1
```

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
