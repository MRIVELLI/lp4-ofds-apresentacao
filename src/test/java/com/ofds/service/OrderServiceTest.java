package com.ofds.service;

import com.ofds.model.*;
import com.ofds.pattern.observer.OrderNotifier;
import com.ofds.repository.MenuItemRepository;
import com.ofds.repository.OrderRepository;
import com.ofds.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private OrderNotifier orderNotifier;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private OrderService orderService;

    private MenuItem itemA;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Restaurant restaurante = new Restaurant("Restaurante 1", "Rua 1", new RestaurantOwner());
        restaurante.setRestaurantId(1L);
        itemA = new MenuItem("Item A", 10.00, true, restaurante);
        MenuItem itemB = new MenuItem("Item B", 25.50, true, restaurante);

        when(restaurantService.getById(1L)).thenReturn(restaurante);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(itemA));
        when(menuItemRepository.findById(2L)).thenReturn(Optional.of(itemB));
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void pedidoComUmItem() {
        Order pedido = fazerPedido(List.of(1L), List.of(1));

        assertEquals(OrderStatus.PLACED, pedido.getStatus());
        assertEquals(10.00, pedido.getTotalAmount(), 0.001);
        assertEquals(PaymentStatus.PENDING, pedido.getPayment().getPaymentStatus());
        assertEquals(10.00, pedido.getPayment().getAmount(), 0.001);
        verify(paymentRepository).save(pedido.getPayment());
        verify(orderNotifier).notifyObservers(pedido);
    }

    @Test
    void pedidoComDoisItens() {
        Order pedido = fazerPedido(List.of(1L, 2L), List.of(2, 1));

        assertEquals(45.50, pedido.getTotalAmount(), 0.001);
    }

    @Test
    void listaDeItensNula() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> fazerPedido(null, List.of(1)));
        assertEquals("A non-empty cart with matching quantities is required.", exception.getMessage());
    }

    @Test
    void listaDeQuantidadesNula() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> fazerPedido(List.of(1L), null));
        assertEquals("A non-empty cart with matching quantities is required.", exception.getMessage());
    }

    @Test
    void carrinhoVazio() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> fazerPedido(List.of(), List.of()));
        assertEquals("A non-empty cart with matching quantities is required.", exception.getMessage());
    }

    @Test
    void quantidadesComUmElementoAMais() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> fazerPedido(List.of(1L), List.of(1, 1)));
        assertEquals("A non-empty cart with matching quantities is required.", exception.getMessage());
    }

    @Test
    void quantidadesComUmElementoAMenos() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> fazerPedido(List.of(1L, 2L), List.of(1)));
        assertEquals("A non-empty cart with matching quantities is required.", exception.getMessage());
    }

    @Test
    void itemIndisponivel() {
        itemA.setAvailability(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> fazerPedido(List.of(1L), List.of(1)));
        assertEquals("Invalid or unavailable menu item in cart.", exception.getMessage());
    }

    @Test
    void itemSemRestaurante() {
        itemA.setRestaurant(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> fazerPedido(List.of(1L), List.of(1)));
        assertEquals("Invalid or unavailable menu item in cart.", exception.getMessage());
    }

    @Test
    void itemDeOutroRestaurante() {
        Restaurant outroRestaurante = new Restaurant();
        outroRestaurante.setRestaurantId(2L);
        itemA.setRestaurant(outroRestaurante);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> fazerPedido(List.of(1L), List.of(1)));
        assertEquals("Invalid or unavailable menu item in cart.", exception.getMessage());
    }

    @Test
    void quantidadeNula() {
        List<Integer> quantidades = Arrays.asList((Integer) null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> fazerPedido(List.of(1L), quantidades));
        assertEquals("Invalid or unavailable menu item in cart.", exception.getMessage());
    }

    @Test
    void quantidadeZero() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> fazerPedido(List.of(1L), List.of(0)));
        assertEquals("Invalid or unavailable menu item in cart.", exception.getMessage());
    }

    @Test
    void itemInexistente() {
        Exception exception = assertThrows(RuntimeException.class, () -> fazerPedido(List.of(99L), List.of(1)));
        assertEquals("Menu item not found", exception.getMessage());
    }

    private Order fazerPedido(List<Long> itens, List<Integer> quantidades) {
        return orderService.placeOrder(new Customer(), 1L, itens, quantidades, PaymentMethod.UPI, restaurantService);
    }
}
