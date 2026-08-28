# TradingSpringSecurity — 專案規則（薄）

繼承：EngineeringOS eos-minimal @ **0.1.13**  
公版：`EngineeringOS/eos-minimal/`  
權威規格：[規格書.md](規格書.md)  
EOS 入口：[TradingSpringSecurity-SPEC.md](TradingSpringSecurity-SPEC.md)

## 與公版差異

- Backend port: 8080
- Framework: Spring Boot 3.2 · Java 21 · Spring Security(JWT) · JPA · H2
- 無 Kafka／Redis（對照 TradingSpringBoot）
- 驗證入口：`.\scripts\check.ps1`（或 `.\gradlew.bat check`）
- 本機 Demo：IntelliJ／終端 **Gradle `bootRun`**（不要用 `*Application` 綠箭）

## 本專案專屬

- Domain: Auth JWT、Order CRUD／冪等、角色 USER／ADMIN
- Architecture: `docs/architecture.md`
- Test: `docs/testing.md`、`docs/testing.md`
- DB／驗證：`docs/資料庫設計.md`、`docs/驗證設計.md`

## 註解深度
- comment_verbosity: **detailed**
- 權威：`EngineeringOS/eos-minimal/knowledge/comments.md` §0／§3b（eos-minimal @ 0.1.13）
- 結構：【職責】【技巧】【概念】；簡單 getter 可併入類別說明


## Git Remote
- 帳號：`johnGitHub24`；一專案一 repo
- 規範：`EngineeringOS/eos-minimal/knowledge/專案上船-GitHub.md`

## 回寫

問題與公版改善建議 → `EngineeringOS/eos-minimal/feedback/SYNC_LOG.md`
