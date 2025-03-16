package com.ncu.fooddelivery.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import com.ncu.fooddelivery.Entities.MenuItem;
import com.ncu.fooddelivery.Entities.Restaurant;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class RestaurantRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public RestaurantRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Restaurant> findById(Long id) {
        String sql = "SELECT * FROM restaurants WHERE id = :id";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), new RestaurantRowMapper())
                .stream()
                .findFirst();
    }

    // Existing method to get menu prices (if needed)
    public Map<Long, BigDecimal> getMenuPrices(Long restaurantId) {
        String sql = "SELECT id, price FROM menu_items WHERE restaurant_id = :restaurantId";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("restaurantId", restaurantId), rs -> {
            Map<Long, BigDecimal> menuPrices = new HashMap<>();
            while (rs.next()) {
                menuPrices.put(rs.getLong("id"), rs.getBigDecimal("price"));
            }
            return menuPrices;
        });
    }
    
    // New method: Get a list of menu items for a restaurant
    public List<MenuItem> getMenuItems(Long restaurantId) {
        String sql = "SELECT * FROM menu_items WHERE restaurant_id = :restaurantId";
        SqlParameterSource params = new MapSqlParameterSource("restaurantId", restaurantId);
        return jdbcTemplate.query(sql, params, new MenuItemRowMapper());
    }

    public int getAveragePrepTime(Long restaurantId) {
        String sql = "SELECT average_prep_time FROM restaurants WHERE id = :restaurantId";
        return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("restaurantId", restaurantId), Integer.class);
    }

    private static class RestaurantRowMapper implements RowMapper<Restaurant> {
        @Override
        public Restaurant mapRow(ResultSet rs, int rowNum) throws SQLException {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(rs.getLong("id"));
            restaurant.setName(rs.getString("name"));
            restaurant.setAddress(rs.getString("address"));
            restaurant.setAveragePrepTime(rs.getInt("average_prep_time"));
            restaurant.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            restaurant.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return restaurant;
        }
    }
    
    private static class MenuItemRowMapper implements RowMapper<MenuItem> {
        @Override
        public MenuItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            MenuItem item = new MenuItem();
            item.setId(rs.getLong("id"));
            item.setRestaurantId(rs.getLong("restaurant_id"));
            item.setName(rs.getString("name"));
            item.setDescription(rs.getString("description"));
            item.setPrice(rs.getBigDecimal("price"));
            item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            item.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return item;
        }
    }
}
