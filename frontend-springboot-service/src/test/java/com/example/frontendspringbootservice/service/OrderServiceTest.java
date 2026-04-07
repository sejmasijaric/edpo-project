package com.example.frontendspringbootservice.service;

import com.example.frontendspringbootservice.dto.CreateOrderRequest;
import com.example.frontendspringbootservice.dto.OrderCreatedEvent;
import com.example.frontendspringbootservice.model.Order;
import com.example.frontendspringbootservice.model.OrderColor;
import com.example.frontendspringbootservice.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(kafkaTemplate, "order-events", "order-created");
    }

    private CreateOrderRequest buildRequest(String id, OrderColor color, String engravedText) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setId(id);
        request.setColor(color);
        request.setEngravedText(engravedText);
        request.setCreatedAt(Instant.parse("2026-04-07T10:00:00Z"));
        return request;
    }

    // --- createOrder ---

    @Test
    void createOrder_storesAndProducesToKafka() {
        CreateOrderRequest request = buildRequest("order-1", OrderColor.RED, null);

        Order result = orderService.createOrder(request);

        assertThat(result.getId()).isEqualTo("order-1");
        assertThat(result.getColor()).isEqualTo(OrderColor.RED);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.TODO);
        assertThat(result.getEngravedText()).isNull();
        assertThat(result.getCreatedAt()).isEqualTo(Instant.parse("2026-04-07T10:00:00Z"));
        verify(kafkaTemplate).send("order-created", "order-1", new OrderCreatedEvent("order-1", "red"));
        verify(kafkaTemplate).send(eq("order-events"), eq("order-1"), any(Object.class));
    }

    @Test
    void createOrder_withEngravedText() {
        CreateOrderRequest request = buildRequest("order-2", OrderColor.BLUE, "Hello");

        Order result = orderService.createOrder(request);

        assertThat(result.getEngravedText()).isEqualTo("Hello");
        verify(kafkaTemplate).send("order-created", "order-2", new OrderCreatedEvent("order-2", "blue"));
        verify(kafkaTemplate).send(eq("order-events"), eq("order-2"), any(Object.class));
    }

    @Test
    void createOrder_nullColorThrows() {
        CreateOrderRequest request = buildRequest("order-3", null, null);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Color is required");
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void createOrder_nullIdThrows() {
        CreateOrderRequest request = buildRequest(null, OrderColor.WHITE, null);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order ID is required");
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void createOrder_blankIdThrows() {
        CreateOrderRequest request = buildRequest("  ", OrderColor.WHITE, null);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order ID is required");
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void createOrder_engravedTextTooLongThrows() {
        CreateOrderRequest request = buildRequest("order-4", OrderColor.RED, "This text is way too long!!");

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Engraved text must be 20 characters or less");
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void createOrder_engravedTextExactly20CharsAllowed() {
        CreateOrderRequest request = buildRequest("order-5", OrderColor.RED, "12345678901234567890");

        Order result = orderService.createOrder(request);

        assertThat(result.getEngravedText()).isEqualTo("12345678901234567890");
    }

    @Test
    void createOrder_defaultsCreatedAtWhenNull() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setId("order-6");
        request.setColor(OrderColor.BLUE);

        Order result = orderService.createOrder(request);

        assertThat(result.getCreatedAt()).isNotNull();
    }

    // --- getAllOrders ---

    @Test
    void getAllOrders_returnsAllStoredOrders() {
        orderService.createOrder(buildRequest("a", OrderColor.RED, null));
        orderService.createOrder(buildRequest("b", OrderColor.BLUE, null));

        List<Order> orders = orderService.getAllOrders();

        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getId).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void getAllOrders_emptyWhenNoOrders() {
        assertThat(orderService.getAllOrders()).isEmpty();
    }

    // --- getOrder ---

    @Test
    void getOrder_returnsExistingOrder() {
        orderService.createOrder(buildRequest("find-me", OrderColor.WHITE, "test"));

        Order result = orderService.getOrder("find-me");

        assertThat(result.getId()).isEqualTo("find-me");
        assertThat(result.getColor()).isEqualTo(OrderColor.WHITE);
    }

    @Test
    void getOrder_notFoundThrows() {
        assertThatThrownBy(() -> orderService.getOrder("nonexistent"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Order not found: nonexistent");
    }

    // --- updateOrderStatus: valid transitions ---

    @Test
    void updateStatus_todoToInProgress() {
        orderService.createOrder(buildRequest("t1", OrderColor.RED, null));
        clearInvocations(kafkaTemplate);

        Order result = orderService.updateOrderStatus("t1", OrderStatus.IN_PROGRESS);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        verify(kafkaTemplate).send(eq("order-events"), eq("t1"), any(Object.class));
    }

    @Test
    void updateStatus_inProgressToDone() {
        orderService.createOrder(buildRequest("t2", OrderColor.RED, null));
        orderService.updateOrderStatus("t2", OrderStatus.IN_PROGRESS);
        clearInvocations(kafkaTemplate);

        Order result = orderService.updateOrderStatus("t2", OrderStatus.DONE);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.DONE);
        verify(kafkaTemplate).send(eq("order-events"), eq("t2"), any(Object.class));
    }

    @Test
    void updateStatus_inProgressToError() {
        orderService.createOrder(buildRequest("t3", OrderColor.RED, null));
        orderService.updateOrderStatus("t3", OrderStatus.IN_PROGRESS);
        clearInvocations(kafkaTemplate);

        Order result = orderService.updateOrderStatus("t3", OrderStatus.ERROR);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.ERROR);
        verify(kafkaTemplate).send(eq("order-events"), eq("t3"), any(Object.class));
    }

    @Test
    void updateStatus_errorToInProgress() {
        orderService.createOrder(buildRequest("t4", OrderColor.RED, null));
        orderService.updateOrderStatus("t4", OrderStatus.IN_PROGRESS);
        orderService.updateOrderStatus("t4", OrderStatus.ERROR);
        clearInvocations(kafkaTemplate);

        Order result = orderService.updateOrderStatus("t4", OrderStatus.IN_PROGRESS);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        verify(kafkaTemplate).send(eq("order-events"), eq("t4"), any(Object.class));
    }

    // --- updateOrderStatus: invalid transitions ---

    @Test
    void updateStatus_doneToAnythingThrows() {
        orderService.createOrder(buildRequest("t5", OrderColor.RED, null));
        orderService.updateOrderStatus("t5", OrderStatus.IN_PROGRESS);
        orderService.updateOrderStatus("t5", OrderStatus.DONE);

        assertThatThrownBy(() -> orderService.updateOrderStatus("t5", OrderStatus.IN_PROGRESS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_todoToDoneThrows() {
        orderService.createOrder(buildRequest("t6", OrderColor.RED, null));

        assertThatThrownBy(() -> orderService.updateOrderStatus("t6", OrderStatus.DONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_todoToErrorThrows() {
        orderService.createOrder(buildRequest("t7", OrderColor.RED, null));

        assertThatThrownBy(() -> orderService.updateOrderStatus("t7", OrderStatus.ERROR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_errorToDoneThrows() {
        orderService.createOrder(buildRequest("t8", OrderColor.RED, null));
        orderService.updateOrderStatus("t8", OrderStatus.IN_PROGRESS);
        orderService.updateOrderStatus("t8", OrderStatus.ERROR);

        assertThatThrownBy(() -> orderService.updateOrderStatus("t8", OrderStatus.DONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_orderNotFoundThrows() {
        assertThatThrownBy(() -> orderService.updateOrderStatus("ghost", OrderStatus.IN_PROGRESS))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Order not found: ghost");
    }
}
