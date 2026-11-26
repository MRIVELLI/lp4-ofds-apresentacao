package com.ofds.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class RestaurantTest {

    @Test
    void integrarRestaurant() {
        RestaurantOwner dono = new RestaurantOwner();
        Restaurant restaurante = new Restaurant("Pizzaria", "Rua 1", dono);
        MenuItem pizza = new MenuItem();
        Order pedidoStub = mock(Order.class);

        restaurante.addMenuItem(pizza);
        restaurante.updateMenuItem(pizza, "Pizza", 30.00, true);
        restaurante.getOrders().add(pedidoStub);

        assertSame(dono, restaurante.getOwner());
        assertSame(restaurante, pizza.getRestaurant());
        assertEquals("Pizza", pizza.getName());
        assertEquals(1, restaurante.getOrders().size());
    }
}
