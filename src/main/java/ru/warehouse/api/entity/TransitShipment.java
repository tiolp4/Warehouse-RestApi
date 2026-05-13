package ru.warehouse.api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transit_shipments")
public class TransitShipment {
    @Id @GeneratedValue
    private UUID id;

    @Column(name = "tracking_number", unique = true, nullable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private String carrier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private String origin;
    @Column(nullable = false)
    private String destination;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;
    @Column(name = "expected_arrival", nullable = false)
    private LocalDate expectedArrival;
    @Column(name = "actual_arrival")
    private LocalDate actualArrival;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status { PLANNED, IN_TRANSIT, DELIVERED, DELAYED, CANCELLED }

    public UUID getId() { return id; }
    public String getTrackingNumber() { return trackingNumber; }
    public String getCarrier() { return carrier; }
    public Supplier getSupplier() { return supplier; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDate getDepartureDate() { return departureDate; }
    public LocalDate getExpectedArrival() { return expectedArrival; }
    public LocalDate getActualArrival() { return actualArrival; }
    public Status getStatus() { return status; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setTrackingNumber(String v) { this.trackingNumber = v; }
    public void setCarrier(String v) { this.carrier = v; }
    public void setSupplier(Supplier v) { this.supplier = v; }
    public void setOrigin(String v) { this.origin = v; }
    public void setDestination(String v) { this.destination = v; }
    public void setDepartureDate(LocalDate v) { this.departureDate = v; }
    public void setExpectedArrival(LocalDate v) { this.expectedArrival = v; }
    public void setActualArrival(LocalDate v) { this.actualArrival = v; }
    public void setStatus(Status v) { this.status = v; }
    public void setNotes(String v) { this.notes = v; }
}
