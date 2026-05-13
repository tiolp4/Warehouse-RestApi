package ru.warehouse.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock")
public class Stock {
    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id", nullable = false)
    private WarehouseCell cell;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public WarehouseCell getCell() { return cell; }
    public Integer getQuantity() { return quantity; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setProduct(Product p)          { this.product = p; }
    public void setCell(WarehouseCell c)       { this.cell = c; }
    public void setQuantity(Integer q)         { this.quantity = q; }
    public void setUpdatedAt(LocalDateTime t)  { this.updatedAt = t; }
}
