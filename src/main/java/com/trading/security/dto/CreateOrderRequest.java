package com.trading.security.dto;

import com.trading.security.domain.OrderSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// ③ API：以 Bean Validation 於 DTO 層驗證
/**
 * 【職責】承載建立訂單 API 的用戶端輸入與格式驗證規則。
 * 【技巧】透過 Jakarta Bean Validation 宣告必填與正數限制，並以 record 提供不可變 DTO。
 * 【概念】在 API 邊界拒絕格式錯誤資料，可讓服務層專注處理業務規則而非重複空值檢查。
 * 【邊界】不包含由伺服器決定的訂單狀態與下單者。
 */
public record CreateOrderRequest(
        @NotBlank(message = "clientOrderId 不可為空")
        String clientOrderId,

        @NotBlank(message = "symbol 不可為空")
        String symbol,

        @NotNull(message = "side 不可為空")
        OrderSide side,

        @NotNull(message = "quantity 不可為空")
        @DecimalMin(value = "0.0", inclusive = false, message = "quantity 必須大於 0")
        BigDecimal quantity,

        @NotNull(message = "price 不可為空")
        @DecimalMin(value = "0.0", inclusive = false, message = "price 必須大於 0")
        BigDecimal price
) {
}
