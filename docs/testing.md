# Testing and Verification — TradingSpringSecurity

> 衝突以 [規格書.md](../規格書.md) 第 7 章為準。  
> Case ID 完整表見 [測試與CI.md](測試與CI.md)。

## Check command

```powershell
.\scripts\check.ps1
```

或直接：

```powershell
.\gradlew.bat check
```

（需 JDK 21；單元 + 整合，H2。）

## Test layers

| Layer | Tag | Gradle | 說明 |
|-------|-----|--------|------|
| 單元 | — | `test` | JWT／Order／Auth／User Service |
| 整合 | `@Tag("integration")` | `integrationTest` | Auth、Security API、DB schema |
| 全部 | — | `check` | 上列 |

## Minimum case types

| Type | Examples |
|------|----------|
| Happy Path | AUTH-001、SEC-001、SEC-005、SEC-007、DB-001 |
| Error Path | AUTH-002／003、SEC-002（401）、SEC-003（400）、SEC-004（409）、SEC-006（403） |

## DoD

- [ ] `gradlew check`／`.\scripts\check.ps1` 全綠
- [ ] 建單 201、無 token 401、驗證 400、冪等 409
- [ ] 非 ADMIN 刪除 403、ADMIN 刪除 204
- [ ] Swagger 與 Actuator health 可存取

詳見 [測試與CI.md](測試與CI.md)。
