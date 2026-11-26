package com.ofds.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuItemTest {

    @Test
    void integrarMenuItem() {
        Restaurant restauranteStub = mock(Restaurant.class);
        when(restauranteStub.getRestaurantId()).thenReturn(1L);

        MenuItem pizza = new MenuItem("Pizza", 30.00, true, restauranteStub);
        pizza.updatePrice(35.00);

        assertEquals(1L, pizza.getRestaurant().getRestaurantId());
        assertEquals(35.00, pizza.getPrice(), 0.001);
    }
}
