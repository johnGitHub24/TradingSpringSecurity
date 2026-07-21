package com.trading.security.repository;

import com.trading.security.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 【職責】提供訂單實體的持久化存取與常用查詢。
 * 【技巧】以 Spring Data 方法命名實作存在性、冪等鍵查找與依使用者分頁查詢。
 * 【概念】把查詢意圖宣告在介面，可減少手寫 SQL／JPQL，同時保持服務層可讀。
 * 【邊界】不含商業規則與授權策略。
 */
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    boolean existsByClientOrderId(String clientOrderId);

    Optional<OrderEntity> findByClientOrderId(String clientOrderId);

    Page<OrderEntity> findByUsername(String username, Pageable pageable);
}
