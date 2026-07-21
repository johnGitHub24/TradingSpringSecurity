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

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 單元測試：覆蓋 {@link com.trading.security.service.OrderService}。
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

    // ORDER-UNIT-001
    @Test
    void createOrder_givenValidRequest_persistsWithStatusNew() {
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

    // ORDER-UNIT-002
    @Test
    void createOrder_givenDuplicateClientOrderId_throwsAndDoesNotSave() {
        when(orderRepository.existsByClientOrderId("cli-001")).thenReturn(true);

        assertThatThrownBy(() -> orderService.createOrder(validRequest(), "alice"))
                .isInstanceOf(DuplicateOrderException.class);

        verify(orderRepository, never()).save(any());
    }

    // ORDER-UNIT-003
    @Test
    void getOrder_whenNotFound_throwsOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ORDER-UNIT-004
    @Test
    void cancelOrder_whenExists_setsStatusCancelled() {
        OrderEntity entity = OrderEntity.builder()
                .id(5L).clientOrderId("cli-005").symbol("ETHUSDT")
                .side(OrderSide.SELL).quantity(new BigDecimal("1"))
                .price(new BigDecimal("3000")).status(OrderStatus.NEW)
                .username("bob").build();
        when(orderRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.cancelOrder(5L);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        ArgumentCaptor<OrderEntity> captor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
