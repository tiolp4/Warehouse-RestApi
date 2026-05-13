package ru.warehouse.api.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
    @Id @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cell_id", nullable = false)
    private WarehouseCell cell;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    public UUID getId() { return id; }
    public Invoice getInvoice() { return invoice; }
    public Product getProduct() { return product; }
    public WarehouseCell getCell() { return cell; }
    public Integer getQuantity() { return quantity; }
    public Integer getActualQuantity() { return actualQuantity; }

    public void setInvoice(Invoice i) { this.invoice = i; }
    public void setProduct(Product p) { this.product = p; }
    public void setCell(WarehouseCell c) { this.cell = c; }
    public void setQuantity(Integer q) { this.quantity = q; }
    public void setActualQuantity(Integer q) { this.actualQuantity = q; }
}
