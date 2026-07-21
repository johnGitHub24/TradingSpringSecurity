package com.trading.security.exception;

/**
 * 【職責】表示建立訂單時 clientOrderId 已存在的業務衝突。
 * 【技巧】以非受檢 {@link RuntimeException} 承載訊息，交由全域處理器轉 HTTP 409。
 * 【概念】用專用例外表達冪等衝突，比回傳 null 或布林更不易被呼叫端忽略。
 * 【邊界】不負責組裝 HTTP 回應。
 */
public class DuplicateOrderException extends RuntimeException {
    public DuplicateOrderException(String clientOrderId) {
        super("重複的 clientOrderId: " + clientOrderId);
    }
}
