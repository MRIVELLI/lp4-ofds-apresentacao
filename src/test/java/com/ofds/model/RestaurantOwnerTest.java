package com.ofds.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RestaurantOwnerTest {

    @Test
    void integrarRestaurantOwner() {
        Restaurant restauranteStub = mock(Restaurant.class);
        when(restauranteStub.getName()).thenReturn("Pizzaria");

        RestaurantOwner dono = new RestaurantOwner("Daniel", "daniel@ofds.com", "11999999999", "pass123");
        dono.getRestaurants().add(restauranteStub);

        assertEquals("daniel@ofds.com", dono.getEmail());
        assertEquals("Pizzaria", dono.getRestaurants().get(0).getName());
    }
}
