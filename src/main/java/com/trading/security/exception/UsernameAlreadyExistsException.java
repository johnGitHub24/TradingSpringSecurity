package com.trading.security.exception;

/**
 * 【職責】表示註冊時帳號已存在。
 * 【技巧】以非受檢例外承載帳號資訊，交由全域處理器轉為 HTTP 409。
 * 【概念】把唯一性衝突提升為明確例外，可讓註冊流程與一般驗證錯誤分開處理。
 * 【邊界】不負責組裝 HTTP 回應。
 */
public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("使用者已存在: " + username);
    }
}
