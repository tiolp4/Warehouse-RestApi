package ru.warehouse.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public final class Dtos {

    private Dtos() {}

    // ── Auth ──────────────────────────────────────────────────
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LoginResponse(String token, long expiresAt, UserDto user) {}
    public record UserDto(UUID id, String username, String fullName, String role) {}

    // ── Suppliers / Products / Stock / Cells ──────────────────
    public record SupplierDto(UUID id, String name, String contact, String phone, String email) {}
    public record ProductDto(UUID id, String sku, String name, String barcode, String unit) {}
    public record StockDto(UUID id, UUID productId, String productName, String sku,
                           UUID cellId, String cellCode, String zone, int quantity) {}
    public record CellDto(UUID id, String code, String zone,
                          Integer rowNum, Integer shelf, Integer capacity, Boolean isOccupied) {}
    public record ActualQtyRequest(@NotNull Integer actualQuantity) {}

    // ── Invoices ──────────────────────────────────────────────
    public record InvoiceDto(UUID id, String invoiceNumber, String type, String status,
                             UUID supplierId, String supplierName,
                             UUID createdBy, String createdByName,
                             LocalDateTime createdAt, LocalDateTime confirmedAt) {}
    public record InvoiceItemDto(UUID id, UUID invoiceId, UUID productId, String productName,
                                 String sku, String barcode,
                                 UUID cellId, String cellCode,
                                 Integer quantity, Integer actualQuantity) {}
    public record InvoiceCreateRequest(
            @NotBlank String invoiceNumber,
            @NotBlank String type,            // INCOMING | OUTGOING
            UUID supplierId) {}
    public record InvoiceItemRequest(
            @NotNull UUID productId,
            @NotNull UUID cellId,
            @NotNull Integer quantity) {}

    // ── Transit shipments ─────────────────────────────────────
    public record TransitDto(
            UUID id, String trackingNumber, String carrier,
            UUID supplierId, String supplierName,
            String origin, String destination,
            LocalDate departureDate, LocalDate expectedArrival, LocalDate actualArrival,
            String status, String notes,
            LocalDateTime createdAt, LocalDateTime updatedAt) {}

    public record TransitCreateRequest(
            @NotBlank String trackingNumber,
            @NotBlank String carrier,
            UUID supplierId,
            @NotBlank String origin,
            @NotBlank String destination,
            @NotNull LocalDate departureDate,
            @NotNull LocalDate expectedArrival,
            String notes) {}

    public record StatusUpdateRequest(@NotBlank String status) {}
    public record MarkArrivedRequest(@NotNull LocalDate actualArrival) {}

    // ── Schedules ─────────────────────────────────────────────
    public record ScheduleDto(
            UUID id, String driverName, String vehicle, String route,
            LocalDate workDate, LocalTime shiftStart, LocalTime shiftEnd,
            String status, UUID shipmentId, String shipmentTracking, String notes) {}

    public record ScheduleCreateRequest(
            @NotBlank String driverName,
            @NotBlank String vehicle,
            @NotBlank String route,
            @NotNull LocalDate workDate,
            @NotNull LocalTime shiftStart,
            @NotNull LocalTime shiftEnd,
            UUID shipmentId,
            String notes) {}

    // ── Analytics ─────────────────────────────────────────────
    public record KpiDto(
            long shipmentsTotal, long shipmentsInTransit, long shipmentsDelivered,
            long shipmentsDelayed, long schedulesTotal, long schedulesToday,
            long schedulesCompleted, long schedulesActive,
            long invoicesTotal, long invoicesConfirmed,
            long qtyIncoming, long qtyOutgoing) {}

    public record DailyFlowDto(LocalDate date, long incoming, long outgoing) {}
    public record CountEntryDto(String key, long count) {}
}
