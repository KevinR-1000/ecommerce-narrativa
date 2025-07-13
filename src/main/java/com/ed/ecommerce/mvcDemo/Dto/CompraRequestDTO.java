package com.ed.ecommerce.mvcDemo.Dto;

import java.util.List;

public class CompraRequestDTO {
    private List<CarritoItemDTO> items;

    // Getter y Setter
    public List<CarritoItemDTO> getItems() { return items; }
    public void setItems(List<CarritoItemDTO> items) { this.items = items; }
}