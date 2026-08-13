package com.trading.security.service;

import com.trading.security.domain.OrderSide;
import com.trading.security.domain.OrderStatus;
import com.trading.security.dto.CreateOrderRequest;
import com.trading.security.dto.OrderResponse;
import com.trading.security.entity.OrderEntity;
import com.trading.security.exception.DuplicateOrderException;
import com.trading.security.exception.OrderNotFoundException;
import com.trading.security.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 【職責】單元測試 {@link OrderService}：建單、冪等、查詢、取消、刪除。
 * 【技巧】Mock OrderRepository，不斷言 HTTP 狀態（由整合層覆蓋）。
 * 【概念】與 ORDER-001, SEC-001 等整合層同一契約：NEW、409 重複、404 不存在、CANCELLED、刪除成功。
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    OrderService orderService;

    private CreateOrderRequest validRequest() {
        return new CreateOrderRequest("cli-001", "BTCUSDT", OrderSide.BUY,
                new BigDecimal("0.5"), new BigDecimal("65000"));
    }

    private OrderEntity existing(long id, OrderStatus status) {
        return OrderEntity.builder()
                .id(id).clientOrderId("cli-005").symbol("ETHUSDT")
                .side(OrderSide.SELL).quantity(new BigDecimal("1"))
                .price(new BigDecimal("3000")).status(status)
                .username("bob").build();
    }

    /**
     * CASE ORDER-001 / SEC-001：合法建單持久化且 status=NEW。
     * Given: clientOrderId 未使用；When: createOrder；Then: 回傳 id 與 NEW（整合層 201）。
     */
    @Test
    void ORDER_001_createOrder_givenValidRequest_persistsWithStatusNew() {
        when(orderRepository.existsByClientOrderId("cli-001")).thenReturn(false);
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        OrderResponse response = orderService.createOrder(validRequest(), "alice");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(OrderStatus.NEW);
        assertThat(response.username()).isEqualTo("alice");
        verify(orderRepository).save(any(OrderEntity.class));
    }

    /**
     * CASE ORDER-002 / SEC-004：重複 clientOrderId 拋 DuplicateOrderException 且不 save。
     * Given: existsByClientOrderId=true；When: createOrder；Then: 衝突例外（整合層 409 DUPLICATE_ORDER）。
     */
    @Test
    void ORDER_002_createOrder_givenDuplicateClientOrderId_throwsAndDoesNotSave() {
        when(orderRepository.existsByClientOrderId("cli-001")).thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder(validRequest(), "alice"))
                .isInstanceOf(DuplicateOrderException.class);

        verify(orderRepository, never()).save(any());
    }

    /**
     * CASE ORDER-003：查無訂單拋 OrderNotFoundException。
     * Given: findById 空；When: getOrder；Then: 不存在例外（整合層 404 ORDER_NOT_FOUND）。
     */
    @Test
    void ORDER_003_getOrder_whenNotFound_throwsOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    /**
     * CASE ORDER-004 / SEC-005：取消既有訂單 → CANCELLED。
     * Given: status=NEW 的訂單；When: cancelOrder；Then: 儲存後狀態為 CANCELLED（整合層 200）。
     */
    @Test
    void ORDER_004_cancelOrder_whenExists_setsStatusCancelled() {
        when(orderRepository.findById(5L)).thenReturn(Optional.of(existing(5L, OrderStatus.NEW)));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.cancelOrder(5L);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    /**
     * CASE ORDER-005：分頁列表把 Entity 映射為 DTO。
     * Given: Repository 回一頁一筆；When: listOrders；Then: 內容與分頁中繼資料保留。
     */
    @Test
    void ORDER_005_listOrders_mapsEntitiesToDtoPage() {
        Page<OrderEntity> page = new PageImpl<>(List.of(existing(5L, OrderStatus.NEW)), PageRequest.of(0, 10), 1);
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<OrderResponse> result = orderService.listOrders(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(5L);
        assertThat(result.getContent().get(0).status()).isEqualTo(OrderStatus.NEW);
    }

    /**
     * CASE SEC-007：刪除前先確認存在再交給 Repository。
     * Given: 訂單存在；When: deleteOrder；Then: 呼叫 delete（整合層 ADMIN 204）。
     */
    @Test
    void SEC_007_deleteOrder_whenExists_deletesEntity() {
        OrderEntity entity = existing(7L, OrderStatus.NEW);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(entity));

        orderService.deleteOrder(7L);

        verify(orderRepository).delete(entity);
    }
}
