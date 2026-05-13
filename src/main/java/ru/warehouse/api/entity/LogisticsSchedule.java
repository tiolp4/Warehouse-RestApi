package ru.warehouse.api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "logistics_schedules")
public class LogisticsSchedule {
    @Id @GeneratedValue
    private UUID id;

    @Column(name = "driver_name", nullable = false)
    private String driverName;
    @Column(nullable = false)
    private String vehicle;
    @Column(nullable = false)
    private String route;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;
    @Column(name = "shift_start", nullable = false)
    private LocalTime shiftStart;
    @Column(name = "shift_end", nullable = false)
    private LocalTime shiftEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private TransitShipment shipment;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status { SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED }

    public UUID getId() { return id; }
    public String getDriverName() { return driverName; }
    public String getVehicle() { return vehicle; }
    public String getRoute() { return route; }
    public LocalDate getWorkDate() { return workDate; }
    public LocalTime getShiftStart() { return shiftStart; }
    public LocalTime getShiftEnd() { return shiftEnd; }
    public Status getStatus() { return status; }
    public TransitShipment getShipment() { return shipment; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setDriverName(String v) { this.driverName = v; }
    public void setVehicle(String v) { this.vehicle = v; }
    public void setRoute(String v) { this.route = v; }
    public void setWorkDate(LocalDate v) { this.workDate = v; }
    public void setShiftStart(LocalTime v) { this.shiftStart = v; }
    public void setShiftEnd(LocalTime v) { this.shiftEnd = v; }
    public void setStatus(Status v) { this.status = v; }
    public void setShipment(TransitShipment v) { this.shipment = v; }
    public void setNotes(String v) { this.notes = v; }
}
