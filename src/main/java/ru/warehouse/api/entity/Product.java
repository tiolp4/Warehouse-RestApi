package ru.warehouse.api.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue
    private UUID id;
    @Column(unique = true, nullable = false)
    private String sku;
    @Column(nullable = false)
    private String name;
    private String barcode;
    private String unit;

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getBarcode() { return barcode; }
    public String getUnit() { return unit; }
}
