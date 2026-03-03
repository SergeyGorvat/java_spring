package spring.mvc.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.mvc.shop.dto.order.CreateOrderDto;
import spring.mvc.shop.dto.order.UpdateOrderStatusDto;
import spring.mvc.shop.entity.Order;
import spring.mvc.shop.entity.OrderItem;
import spring.mvc.shop.entity.Status;
import spring.mvc.shop.entity.User;
import spring.mvc.shop.exception.OrderNotFoundException;
import spring.mvc.shop.exception.UserNotFoundException;
import spring.mvc.shop.repository.OrderRepository;
import spring.mvc.shop.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public List<Order> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return orderRepository.findByUserIdWithItems(userId);
    }

    public Order findById(Long id) {
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional
    public Order create(Long userId, CreateOrderDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Order order = Order.builder()
                .user(user)
                .status(Status.NEW)
                .build();

        List<OrderItem> items = dto.getItems().stream()
                .map(itemDto -> OrderItem.builder()
                        .productName(itemDto.getProductName())
                        .quantity(itemDto.getQuantity())
                        .price(itemDto.getPrice())
                        .order(order)
                        .build())
                .toList();

        order.getItems().addAll(items);
        order.recalculateTotalAmount();

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long id, UpdateOrderStatusDto dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        Status newStatus;
        try {
            newStatus = Status.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверный статус: " + dto.getStatus() +
                                               ". Допустимые значения: NEW, CONFIRMED, SHIPPED, DELIVERED, CANCELLED");
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }
        orderRepository.deleteById(id);
    }
}
