package com.ofds.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderTest {

    @Test
    void addItemRecalculatesTotalFromMenuPriceSnapshot() {
        Restaurant restaurant = new Restaurant("Test Restaurant", "Test Address", new RestaurantOwner());
        MenuItem item = new MenuItem("Test Item", 12.50, true, restaurant);
        Order order = new Order(new Customer(), restaurant);

        order.addItem(new OrderItem(item, 2));

        assertEquals(25.0, order.getTotalAmount(), 0.001);
    }

    @Test
    void integrarOrder() {
        Customer clienteStub = mock(Customer.class);
        DeliveryPartner entregadorStub = mock(DeliveryPartner.class);
        Payment pagamentoStub = mock(Payment.class);
        OrderItem itemStub1 = mock(OrderItem.class);
        OrderItem itemStub2 = mock(OrderItem.class);
        when(itemStub1.getSubtotal()).thenReturn(20.00);
        when(itemStub2.getSubtotal()).thenReturn(15.50);

        Order pedido = new Order(clienteStub, new Restaurant());
        pedido.setDeliveryPartner(entregadorStub);
        pedido.setPayment(pagamentoStub);
        pedido.addItem(itemStub1);
        pedido.addItem(itemStub2);

        assertEquals(35.50, pedido.getTotalAmount(), 0.001);
        verify(itemStub1).setOrder(pedido);
        verify(itemStub2).setOrder(pedido);
    }
}
