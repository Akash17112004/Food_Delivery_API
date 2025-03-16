package com.ncu.fooddelivery.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ncu.fooddelivery.Entities.*;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Service
public class DiscountService {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Autowired
	public DiscountService(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public BigDecimal applyDiscount(String code, BigDecimal totalAmount) {
		// Fetch discount code details
		String sql = "SELECT * FROM discount_codes WHERE code = :code "
				+ "AND valid_from <= NOW() AND valid_until >= NOW() "
				+ "AND (max_uses IS NULL OR used_count < max_uses) FOR UPDATE";

		DiscountCode discount = jdbcTemplate
				.query(sql, new MapSqlParameterSource("code", code), new DiscountCodeRowMapper()).stream().findFirst()
				.orElseThrow(() -> new RuntimeException("Invalid or expired discount code"));

		BigDecimal discountAmount = calculateDiscount(discount, totalAmount);

		String updateSql = "UPDATE discount_codes SET used_count = used_count + 1 WHERE code = :code";
		jdbcTemplate.update(updateSql, new MapSqlParameterSource("code", code));

		return discountAmount;
	}

	private BigDecimal calculateDiscount(DiscountCode discount, BigDecimal total) {
		return discount.getDiscountType() == DiscountCode.DiscountType.PERCENT
				? total.multiply(discount.getDiscountValue().divide(BigDecimal.valueOf(100)))
				: discount.getDiscountValue().min(total);
	}

	private static class DiscountCodeRowMapper implements RowMapper<DiscountCode> {
		@Override
		public DiscountCode mapRow(ResultSet rs, int rowNum) throws SQLException {
			DiscountCode code = new DiscountCode();
			code.setCode(rs.getString("code"));
			code.setDiscountType(DiscountCode.DiscountType.valueOf(rs.getString("discount_type")));
			code.setDiscountValue(rs.getBigDecimal("discount_value"));
			code.setValidFrom(rs.getTimestamp("valid_from").toLocalDateTime());
			code.setValidUntil(rs.getTimestamp("valid_until").toLocalDateTime());
			code.setMaxUses(rs.getInt("max_uses"));
			code.setUsedCount(rs.getInt("used_count"));
			return code;
		}
	}
}