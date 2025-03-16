package com.ncu.fooddelivery.Entities;

import java.time.LocalDateTime;

public class Restaurant {
    private Long id;
    private String name;
    private String address;
    private int averagePrepTime; // in minutes
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getAveragePrepTime() {
		return averagePrepTime;
	}
	public void setAveragePrepTime(int averagePrepTime) {
		this.averagePrepTime = averagePrepTime;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

    // Getters and Setters
}
