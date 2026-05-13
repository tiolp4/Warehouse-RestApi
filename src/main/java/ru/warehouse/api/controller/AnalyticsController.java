package ru.warehouse.api.controller;

import org.springframework.web.bind.annotation.*;
import ru.warehouse.api.dto.Dtos.*;
import ru.warehouse.api.service.AnalyticsService;

import java.util.List;

@RestController
@RequestMapping("/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService svc;
    public AnalyticsController(AnalyticsService svc) { this.svc = svc; }

    @GetMapping("/kpi")
    public KpiDto kpi() { return svc.kpi(); }

    @GetMapping("/invoice-flow")
    public List<DailyFlowDto> invoiceFlow(@RequestParam(defaultValue = "14") int days) {
        return svc.invoiceFlow(days);
    }

    @GetMapping("/shipment-flow")
    public List<DailyFlowDto> shipmentFlow(@RequestParam(defaultValue = "14") int days) {
        return svc.shipmentFlow(days);
    }

    @GetMapping("/transit-status")
    public List<CountEntryDto> transitStatus() { return svc.transitByStatus(); }

    @GetMapping("/schedule-status")
    public List<CountEntryDto> scheduleStatus() { return svc.scheduleByStatus(); }

    @GetMapping("/top-carriers")
    public List<CountEntryDto> topCarriers(@RequestParam(defaultValue = "5") int limit) {
        return svc.topCarriers(limit);
    }

    @GetMapping("/top-drivers")
    public List<CountEntryDto> topDrivers(@RequestParam(defaultValue = "5") int limit) {
        return svc.topDrivers(limit);
    }
}
