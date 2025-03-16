package com.ncu.fooddelivery.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ncu.fooddelivery.Entities.MenuItem;
import com.ncu.fooddelivery.Repository.RestaurantRepository;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final RestaurantRepository restaurantRepository;

    @Autowired
    public RestaurantController(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<List<MenuItem>> getMenu(@PathVariable Long restaurantId) {
        List<MenuItem> menu = restaurantRepository.getMenuItems(restaurantId);
        return ResponseEntity.ok(menu);
    }
}

