package com.trading.security.dto;

import com.trading.security.domain.OrderSide;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】單元測試 {@link CreateOrderRequest} 的 Bean Validation 約束。
 * 【技巧】純 Jakarta Validator（無 Spring 容器）。
 * 【概念】與 SEC-003 整合層同一契約：非法欄位在進 Service 前被拒（HTTP 400 VALIDATION_FAILED）。
 */
class CreateOrderRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    private CreateOrderRequest valid() {
        return new CreateOrderRequest("cli-001", "BTCUSDT", OrderSide.BUY,
                new BigDecimal("0.5"), new BigDecimal("65000"));
    }

    /**
     * CASE ORDER-001 / SEC-001：合法請求無違規。
     * Given: 完整合法欄位；When: validate；Then: violations 為空。
     */
    @Test
    void ORDER_001_validRequest_hasNoViolations() {
        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(valid());
        assertThat(violations).isEmpty();
    }

    /**
     * CASE SEC-003：空白 clientOrderId 與非正數 quantity／price 有違規。
     * Given: 空冪等鍵且 quantity／price 非法；When: validate；Then: 對應欄位違規（整合層 400）。
     */
    @Test
    void SEC_003_invalidBody_hasViolations() {
        CreateOrderRequest request = new CreateOrderRequest(
                "", "BTCUSDT", OrderSide.BUY, new BigDecimal("-1"), BigDecimal.ZERO);

        Set<ConstraintViolation<CreateOrderRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("clientOrderId"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }
}
