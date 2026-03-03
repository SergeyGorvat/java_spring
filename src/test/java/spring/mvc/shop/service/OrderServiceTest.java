package spring.mvc.shop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spring.mvc.shop.dto.order.CreateOrderDto;
import spring.mvc.shop.dto.order.OrderItemDto;
import spring.mvc.shop.dto.order.UpdateOrderStatusDto;
import spring.mvc.shop.entity.Order;
import spring.mvc.shop.entity.Status;
import spring.mvc.shop.entity.User;
import spring.mvc.shop.exception.OrderNotFoundException;
import spring.mvc.shop.exception.UserNotFoundException;
import spring.mvc.shop.repository.OrderRepository;
import spring.mvc.shop.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Сергей")
                .email("gorvat@example.com")
                .build();

        testOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .status(Status.NEW)
                .totalAmount(new BigDecimal("1000.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("findByUserId — пользователь не найден → UserNotFoundException")
    void findByUserId_UserNotFound_ThrowsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.findByUserId(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("findByUserId — возвращает список заказов пользователя")
    void findByUserId_UserExists_ReturnsOrders() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(orderRepository.findByUserIdWithItems(1L)).thenReturn(List.of(testOrder));

        List<Order> result = orderService.findByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Status.NEW);
    }

    @Test
    @DisplayName("create — создаёт заказ с правильной суммой")
    void create_ValidRequest_CreatesOrderWithCorrectTotal() {
        CreateOrderDto dto = new CreateOrderDto(List.of(
                new OrderItemDto("Товар 1", 2, new BigDecimal("500.00")),
                new OrderItemDto("Товар 2", 1, new BigDecimal("300.00"))
        ));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.create(1L, dto);

        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1300.00"));
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getStatus()).isEqualTo(Status.NEW);
    }

    @Test
    @DisplayName("updateStatus — успешное обновление статуса")
    void updateStatus_ValidStatus_UpdatesOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateOrderStatusDto dto = new UpdateOrderStatusDto("CONFIRMED");
        Order result = orderService.updateStatus(1L, dto);

        assertThat(result.getStatus()).isEqualTo(Status.CONFIRMED);
    }

    @Test
    @DisplayName("updateStatus — неверный статус → IllegalArgumentException")
    void updateStatus_InvalidStatus_ThrowsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        UpdateOrderStatusDto dto = new UpdateOrderStatusDto("INVALID_STATUS");

        assertThatThrownBy(() -> orderService.updateStatus(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INVALID_STATUS");
    }

    @Test
    @DisplayName("delete — заказ не найден → OrderNotFoundException")
    void delete_OrderNotFound_ThrowsException() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> orderService.delete(99L))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).deleteById(any());
    }
}