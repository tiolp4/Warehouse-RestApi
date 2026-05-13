package ru.warehouse.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id @GeneratedValue
    private UUID id;

    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    public enum Type { INCOMING, OUTGOING }
    public enum Status { DRAFT, CONFIRMED, CANCELLED }

    public UUID getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Type getType() { return type; }
    public Status getStatus() { return status; }
    public Supplier getSupplier() { return supplier; }
    public User getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }

    public void setInvoiceNumber(String n) { this.invoiceNumber = n; }
    public void setType(Type t) { this.type = t; }
    public void setStatus(Status s) { this.status = s; }
    public void setSupplier(Supplier s) { this.supplier = s; }
    public void setCreatedBy(User u) { this.createdBy = u; }
    public void setConfirmedAt(LocalDateTime t) { this.confirmedAt = t; }
}
