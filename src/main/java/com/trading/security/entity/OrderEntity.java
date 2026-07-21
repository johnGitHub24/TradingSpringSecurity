package com.trading.security.entity;

import com.trading.security.domain.OrderSide;
import com.trading.security.domain.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 【職責】映射訂單資料表 {@code orders}，保存下單欄位與審計時間戳。
 * 【技巧】以 JPA 註解定義唯一索引與常查欄位索引，並透過 {@link AuditingEntityListener} 自動填寫建立／更新時間。
 * 【概念】實體只描述持久化結構；商業規則與 API 形狀應留在 Service／DTO，避免 Entity 外洩造成耦合。
 * 【邊界】不含商業規則與 DTO 轉換。
 */
@Entity
@Table(name = "orders", indexes = {
        // ② 資料層：冪等鍵與常查欄位建立索引
        @Index(name = "uk_orders_client_order_id", columnList = "client_order_id", unique = true),
        @Index(name = "idx_orders_symbol", columnList = "symbol"),
        @Index(name = "idx_orders_username", columnList = "username")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_order_id", nullable = false, unique = true, length = 100)
    private String clientOrderId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private OrderSide side;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, length = 100)
    private String username;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
