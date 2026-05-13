package ru.warehouse.api.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.warehouse.api.dto.Dtos.*;
import ru.warehouse.api.service.InvoiceService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/invoices")
public class InvoiceController {

    private final InvoiceService svc;
    public InvoiceController(InvoiceService svc) { this.svc = svc; }

    @GetMapping
    public List<InvoiceDto> list() { return svc.listAll(); }

    @GetMapping("/{id}/items")
    public List<InvoiceItemDto> items(@PathVariable UUID id) { return svc.listItems(id); }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public InvoiceDto create(@Valid @RequestBody InvoiceCreateRequest req,
                             @AuthenticationPrincipal String username) {
        return svc.create(req, username);
    }

    @PostMapping("/{id}/items")
    public InvoiceItemDto addItem(@PathVariable UUID id,
                                  @Valid @RequestBody InvoiceItemRequest req) {
        return svc.addItem(id, req);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('MANAGER')")
    public InvoiceDto confirm(@PathVariable UUID id) { return svc.confirm(id); }

    @PatchMapping("/items/{itemId}/actual")
    public InvoiceItemDto setActual(@PathVariable UUID itemId,
                                    @Valid @RequestBody ActualQtyRequest req) {
        return svc.setActualQuantity(itemId, req.actualQuantity());
    }
}
