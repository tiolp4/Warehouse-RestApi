package ru.warehouse.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.warehouse.api.dto.Dtos.*;
import ru.warehouse.api.entity.LogisticsSchedule;
import ru.warehouse.api.entity.Supplier;
import ru.warehouse.api.entity.TransitShipment;
import ru.warehouse.api.exception.ApiException;
import ru.warehouse.api.repository.ScheduleRepository;
import ru.warehouse.api.repository.SupplierRepository;
import ru.warehouse.api.repository.TransitShipmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LogisticsService {

    private final TransitShipmentRepository transitRepo;
    private final ScheduleRepository scheduleRepo;
    private final SupplierRepository supplierRepo;

    public LogisticsService(TransitShipmentRepository t, ScheduleRepository s, SupplierRepository sup) {
        this.transitRepo = t; this.scheduleRepo = s; this.supplierRepo = sup;
    }

    // ── Transit ───────────────────────────────────────────────

    public List<TransitDto> listShipments() {
        return transitRepo.findAllOrdered().stream().map(this::toDto).toList();
    }

    @Transactional
    public TransitDto createShipment(TransitCreateRequest req) {
        if (req.expectedArrival().isBefore(req.departureDate()))
            throw ApiException.badRequest("expectedArrival before departureDate");
        TransitShipment t = new TransitShipment();
        t.setTrackingNumber(req.trackingNumber());
        t.setCarrier(req.carrier());
        if (req.supplierId() != null) {
            Supplier sup = supplierRepo.findById(req.supplierId())
                    .orElseThrow(() -> ApiException.notFound("Supplier not found"));
            t.setSupplier(sup);
        }
        t.setOrigin(req.origin());
        t.setDestination(req.destination());
        t.setDepartureDate(req.departureDate());
        t.setExpectedArrival(req.expectedArrival());
        t.setNotes(req.notes());
        return toDto(transitRepo.save(t));
    }

    @Transactional
    public TransitDto changeStatus(UUID id, String status) {
        TransitShipment t = transitRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Shipment not found"));
        t.setStatus(TransitShipment.Status.valueOf(status));
        return toDto(t);
    }

    @Transactional
    public TransitDto markArrived(UUID id, LocalDate arrival) {
        TransitShipment t = transitRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Shipment not found"));
        t.setActualArrival(arrival);
        t.setStatus(TransitShipment.Status.DELIVERED);
        return toDto(t);
    }

    @Transactional
    public void deleteShipment(UUID id) { transitRepo.deleteById(id); }

    private TransitDto toDto(TransitShipment t) {
        return new TransitDto(
                t.getId(), t.getTrackingNumber(), t.getCarrier(),
                t.getSupplier() == null ? null : t.getSupplier().getId(),
                t.getSupplier() == null ? null : t.getSupplier().getName(),
                t.getOrigin(), t.getDestination(),
                t.getDepartureDate(), t.getExpectedArrival(), t.getActualArrival(),
                t.getStatus().name(), t.getNotes(),
                t.getCreatedAt(), t.getUpdatedAt());
    }

    // ── Schedules ─────────────────────────────────────────────

    public List<ScheduleDto> listSchedules(LocalDate from, LocalDate to) {
        var list = (from != null && to != null)
                ? scheduleRepo.findInRange(from, to)
                : scheduleRepo.findAllOrdered();
        return list.stream().map(this::toDto).toList();
    }

    @Transactional
    public ScheduleDto createSchedule(ScheduleCreateRequest req) {
        if (req.shiftEnd().isBefore(req.shiftStart()))
            throw ApiException.badRequest("shiftEnd before shiftStart");
        LogisticsSchedule s = new LogisticsSchedule();
        s.setDriverName(req.driverName());
        s.setVehicle(req.vehicle());
        s.setRoute(req.route());
        s.setWorkDate(req.workDate());
        s.setShiftStart(req.shiftStart());
        s.setShiftEnd(req.shiftEnd());
        s.setNotes(req.notes());
        if (req.shipmentId() != null) {
            TransitShipment ts = transitRepo.findById(req.shipmentId())
                    .orElseThrow(() -> ApiException.notFound("Shipment not found"));
            s.setShipment(ts);
        }
        return toDto(scheduleRepo.save(s));
    }

    @Transactional
    public ScheduleDto changeScheduleStatus(UUID id, String status) {
        LogisticsSchedule s = scheduleRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Schedule not found"));
        s.setStatus(LogisticsSchedule.Status.valueOf(status));
        return toDto(s);
    }

    @Transactional
    public void deleteSchedule(UUID id) { scheduleRepo.deleteById(id); }

    private ScheduleDto toDto(LogisticsSchedule s) {
        return new ScheduleDto(
                s.getId(), s.getDriverName(), s.getVehicle(), s.getRoute(),
                s.getWorkDate(), s.getShiftStart(), s.getShiftEnd(),
                s.getStatus().name(),
                s.getShipment() == null ? null : s.getShipment().getId(),
                s.getShipment() == null ? null : s.getShipment().getTrackingNumber(),
                s.getNotes());
    }
}
