package com.ncu.fooddelivery.Service;

import com.ncu.fooddelivery.Entities.Order;
import com.ncu.fooddelivery.Entities.OrderItem;
import com.ncu.fooddelivery.Entities.OrderRequest;
import com.ncu.fooddelivery.Exception.ResourceNotFoundException;
import com.ncu.fooddelivery.Repository.OrderRepository;
import com.ncu.fooddelivery.Repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final DiscountService discountService;

    @Autowired
    public OrderService(OrderRepository orderRepository, RestaurantRepository restaurantRepository, DiscountService discountService) {
        this.orderRepository = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.discountService = discountService;
    }

    @Transactional
    public Order placeOrder(OrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setRestaurantId(request.getRestaurantId());
        order.setDiscountCode(request.getDiscountCode());
        order.setStatus(Order.OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (var itemRequest : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(itemRequest.getItemId());
            orderItem.setQuantity(itemRequest.getQuantity());
            BigDecimal price = new BigDecimal("10.00"); 
            orderItem.setPrice(price);
            totalAmount = totalAmount.add(price.multiply(new BigDecimal(itemRequest.getQuantity())));
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        if (order.getDiscountCode() != null && !order.getDiscountCode().isEmpty()) {
            BigDecimal discountAmount = discountService.applyDiscount(order.getDiscountCode(), totalAmount);
            order.setDiscountAmount(discountAmount);
            order.setTotalAmount(totalAmount.subtract(discountAmount));
        }
        
        restaurantRepository.findById(order.getRestaurantId()).ifPresent(restaurant -> {
            order.setDeliveryTime(LocalDateTime.now().plusMinutes(restaurant.getAveragePrepTime()));
        });
        
        return orderRepository.createOrder(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        orderRepository.updateStatus(orderId, Order.OrderStatus.CANCELLED);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    public List<Order> getUserOrders(Long userId, int page, int size) {
        return orderRepository.getOrdersByUser(userId, page, size);
    }
    
    public List<Order> getAllOrders(int page, int size) {
        return orderRepository.getAllOrders(page, size);
    }
}
