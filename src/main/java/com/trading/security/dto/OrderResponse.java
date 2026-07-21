package com.trading.security.dto;

import com.trading.security.domain.OrderSide;
import com.trading.security.domain.OrderStatus;
import com.trading.security.entity.OrderEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 【職責】作為訂單 API 對外回應的不可變表示，並可序列化以支援快取。
 * 【技巧】實作 {@link Serializable}，並以 {@link #from(OrderEntity)} 自 JPA 實體轉換。
 * 【概念】API 不直接回傳 Entity，可避免懶加載意外與持久化細節外洩。
 * 【邊界】不含寫入行為或驗證規則。
 */
public record OrderResponse(
        Long id,
        String clientOrderId,
        String symbol,
        OrderSide side,
        BigDecimal quantity,
        BigDecimal price,
        OrderStatus status,
        String username,
        Instant createdAt
) implements Serializable {

    /**
     * 【職責】將訂單實體轉換為 API 回應 DTO。
     * 【技巧】逐欄複製需要對外暴露的屬性，忽略持久化內部細節。
     * 【概念】集中轉換點可讓欄位增刪時只改一處，降低 Controller／Service 重複映射。
     * @param entity 訂單實體
     * @return API 回應
     */
    public static OrderResponse from(OrderEntity entity) {
        return new OrderResponse(
                entity.getId(),
                entity.getClientOrderId(),
                entity.getSymbol(),
                entity.getSide(),
                entity.getQuantity(),
                entity.getPrice(),
                entity.getStatus(),
                entity.getUsername(),
                entity.getCreatedAt()
        );
    }
}
