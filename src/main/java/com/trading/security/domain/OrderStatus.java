package com.trading.security.domain;

/**
 * 【職責】定義訂單生命週期中可持久化的狀態。
 * 【技巧】以 {@link com.trading.security.entity.OrderEntity} 的 {@code EnumType.STRING} 保存列舉名稱。
 * 【概念】狀態列舉集中表達有限生命週期，較自由字串更容易驗證與維護資料相容性。
 * 【邊界】不負責判定何時可轉換狀態；規則由服務層決定。
 */
public enum OrderStatus {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED
}
