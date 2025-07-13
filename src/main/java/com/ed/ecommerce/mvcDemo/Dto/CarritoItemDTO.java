package com.ed.ecommerce.mvcDemo.Dto;

import java.math.BigDecimal;

public class CarritoItemDTO {
    private Integer productId;
    private int quantity;

    // --- NUEVOS CAMPOS ---
    private String name;
    private BigDecimal price;

    // Getters y Setters para todos los campos

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}