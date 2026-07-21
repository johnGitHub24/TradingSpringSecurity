package com.trading.security.domain;

/**
 * 【職責】表示訂單的買入或賣出方向。
 * 【技巧】以 enum 提供型別安全的有限值，供 DTO 與 JPA 實體共用。
 * 【概念】相較於任意字串，enum 可在編譯期限制合法方向，避免拼字錯誤進入交易流程。
 * 【邊界】不包含價格、數量或成交狀態。
 */
public enum OrderSide {
    BUY,
    SELL
}
