package ru.warehouse.api.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.warehouse.api.dto.Dtos.*;
import ru.warehouse.api.service.LogisticsService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@PreAuthorize("hasRole('MANAGER')")
public class LogisticsController {

    private final LogisticsService svc;
    public LogisticsController(LogisticsService svc) { this.svc = svc; }

    // ── Shipments ─────────────────────────────────────────────

    @GetMapping("/shipments")
    public List<TransitDto> shipments() { return svc.listShipments(); }

    @PostMapping("/shipments")
    public TransitDto createShipment(@Valid @RequestBody TransitCreateRequest req) {
        return svc.createShipment(req);
    }

    @PatchMapping("/shipments/{id}/status")
    public TransitDto changeStatus(@PathVariable UUID id,
                                   @Valid @RequestBody StatusUpdateRequest req) {
        return svc.changeStatus(id, req.status());
    }

    @PatchMapping("/shipments/{id}/arrived")
    public TransitDto markArrived(@PathVariable UUID id,
                                  @Valid @RequestBody MarkArrivedRequest req) {
        return svc.markArrived(id, req.actualArrival());
    }

    @DeleteMapping("/shipments/{id}")
    public void deleteShipment(@PathVariable UUID id) { svc.deleteShipment(id); }

    // ── Schedules ─────────────────────────────────────────────

    @GetMapping("/schedules")
    public List<ScheduleDto> schedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return svc.listSchedules(from, to);
    }

    @PostMapping("/schedules")
    public ScheduleDto createSchedule(@Valid @RequestBody ScheduleCreateRequest req) {
        return svc.createSchedule(req);
    }

    @PatchMapping("/schedules/{id}/status")
    public ScheduleDto changeScheduleStatus(@PathVariable UUID id,
                                            @Valid @RequestBody StatusUpdateRequest req) {
        return svc.changeScheduleStatus(id, req.status());
    }

    @DeleteMapping("/schedules/{id}")
    public void deleteSchedule(@PathVariable UUID id) { svc.deleteSchedule(id); }
}
