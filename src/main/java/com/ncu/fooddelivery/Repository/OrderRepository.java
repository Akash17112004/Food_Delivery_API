package com.ncu.fooddelivery.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ncu.fooddelivery.Entities.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	public OrderRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public Order createOrder(Order order) {
		String orderSql = "INSERT INTO orders (user_id, restaurant_id, status, delivery_time, total_amount, discount_code, discount_amount) "
				+ "VALUES (:userId, :restaurantId, :status, :deliveryTime, :totalAmount, :discountCode, :discountAmount)";

		SqlParameterSource orderParams = new MapSqlParameterSource().addValue("userId", order.getUserId())
				.addValue("restaurantId", order.getRestaurantId()).addValue("status", order.getStatus().name())
				.addValue("deliveryTime", order.getDeliveryTime()).addValue("totalAmount", order.getTotalAmount())
				.addValue("discountCode", order.getDiscountCode())
				.addValue("discountAmount", order.getDiscountAmount());

		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(orderSql, orderParams, keyHolder, new String[] { "id" });
		Long orderId = keyHolder.getKey().longValue();
		order.setId(orderId);

		String itemsSql = "INSERT INTO order_items (order_id, item_id, quantity, price) "
				+ "VALUES (:orderId, :itemId, :quantity, :price)";

		List<SqlParameterSource> itemParams = order.getItems().stream()
				.map(item -> new MapSqlParameterSource().addValue("orderId", orderId)
						.addValue("itemId", item.getItemId()).addValue("quantity", item.getQuantity())
						.addValue("price", item.getPrice()))
				.collect(Collectors.toList());

		jdbcTemplate.batchUpdate(itemsSql, itemParams.toArray(new SqlParameterSource[0]));
		return order;
	}

	public Optional<Order> findById(Long id) {
		String sql = "SELECT o.*, oi.item_id, oi.quantity, oi.price "
				+ "FROM orders o LEFT JOIN order_items oi ON o.id = oi.order_id " + "WHERE o.id = :id";
		return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), new OrderResultSetExtractor());
	}

	public void updateStatus(Long orderId, Order.OrderStatus status) {
		String sql = "UPDATE orders SET status = :status WHERE id = :id";
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", orderId).addValue("status",
				status.name());
		jdbcTemplate.update(sql, params);
	}

	public List<Order> getOrdersByUser(Long userId, int page, int size) {
		String sql = "SELECT * FROM orders WHERE user_id = :userId "
				+ "ORDER BY delivery_time ASC LIMIT :size OFFSET :offset";
		SqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId).addValue("size", size)
				.addValue("offset", (page - 1) * size);
		return jdbcTemplate.query(sql, params, new OrderRowMapper());
	}

	public List<Order> getAllOrders(int page, int size) {
		String sql = "SELECT * FROM orders ORDER BY delivery_time ASC LIMIT :size OFFSET :offset";
		SqlParameterSource params = new MapSqlParameterSource().addValue("size", size).addValue("offset",
				(page - 1) * size);
		return jdbcTemplate.query(sql, params, new OrderRowMapper());
	}

	private static class OrderResultSetExtractor implements ResultSetExtractor<Optional<Order>> {
		@Override
		public Optional<Order> extractData(ResultSet rs) throws SQLException {
			Order order = null;
			while (rs.next()) {
				if (order == null) {
					order = new OrderRowMapper().mapRow(rs, 0);
				}
				if (rs.getLong("item_id") > 0) {
					OrderItem item = new OrderItem();
					item.setItemId(rs.getLong("item_id"));
					item.setQuantity(rs.getInt("quantity"));
					item.setPrice(rs.getBigDecimal("price"));
					order.getItems().add(item);
				}
			}
			return Optional.ofNullable(order);
		}
	}

	private static class OrderRowMapper implements RowMapper<Order> {
		@Override
		public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
			Order order = new Order();
			order.setId(rs.getLong("id"));
			order.setUserId(rs.getLong("user_id"));
			order.setRestaurantId(rs.getLong("restaurant_id"));
			order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
			order.setDeliveryTime(rs.getTimestamp("delivery_time").toLocalDateTime());
			order.setTotalAmount(rs.getBigDecimal("total_amount"));
			order.setDiscountCode(rs.getString("discount_code"));
			order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
			order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
			order.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
			order.setItems(new ArrayList<>());
			return order;
		}
	}
}
