package com.trading.security.exception;

/**
 * 【職責】表示依主鍵查詢時找不到訂單。
 * 【技巧】以非受檢例外攜帶訂單 ID 訊息，供全域處理器轉為 HTTP 404。
 * 【概念】明確的領域例外讓服務方法可用「找不到就失敗」表達契約，不必到處回 Optional 到 Controller。
 * 【邊界】不負責組裝 HTTP 回應。
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("找不到訂單: " + id);
    }
}
