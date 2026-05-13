package ru.warehouse.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.warehouse.api.dto.Dtos.*;
import ru.warehouse.api.entity.*;
import ru.warehouse.api.exception.ApiException;
import ru.warehouse.api.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InvoiceService {

    private final InvoiceRepository invoices;
    private final InvoiceItemRepository items;
    private final ProductRepository products;
    private final CellRepository cells;
    private final SupplierRepository suppliers;
    private final UserRepository users;
    private final StockRepository stocks;

    public InvoiceService(InvoiceRepository invoices, InvoiceItemRepository items,
                          ProductRepository products, CellRepository cells,
                          SupplierRepository suppliers, UserRepository users,
                          StockRepository stocks) {
        this.invoices = invoices; this.items = items; this.products = products;
        this.cells = cells; this.suppliers = suppliers; this.users = users;
        this.stocks = stocks;
    }

    public List<InvoiceDto> listAll() {
        return invoices.findAllOrdered().stream().map(this::toDto).toList();
    }

    public List<InvoiceItemDto> listItems(UUID invoiceId) {
        return items.findByInvoiceId(invoiceId).stream().map(this::itemDto).toList();
    }

    @Transactional
    public InvoiceDto create(InvoiceCreateRequest req, String username) {
        Invoice inv = new Invoice();
        inv.setInvoiceNumber(req.invoiceNumber());
        inv.setType(Invoice.Type.valueOf(req.type()));
        if (req.supplierId() != null) {
            Supplier sup = suppliers.findById(req.supplierId())
                    .orElseThrow(() -> ApiException.notFound("Supplier not found"));
            inv.setSupplier(sup);
        }
        users.findByUsername(username).ifPresent(inv::setCreatedBy);
        return toDto(invoices.save(inv));
    }

    @Transactional
    public InvoiceItemDto addItem(UUID invoiceId, InvoiceItemRequest req) {
        Invoice inv = invoices.findById(invoiceId)
                .orElseThrow(() -> ApiException.notFound("Invoice not found"));
        Product p = products.findById(req.productId())
                .orElseThrow(() -> ApiException.notFound("Product not found"));
        WarehouseCell c = cells.findById(req.cellId())
                .orElseThrow(() -> ApiException.notFound("Cell not found"));
        InvoiceItem it = new InvoiceItem();
        it.setInvoice(inv); it.setProduct(p); it.setCell(c);
        it.setQuantity(req.quantity());
        return itemDto(items.save(it));
    }

    @Transactional
    public InvoiceItemDto setActualQuantity(UUID itemId, int actual) {
        InvoiceItem it = items.findById(itemId)
                .orElseThrow(() -> ApiException.notFound("Item not found"));
        if (actual < 0) throw ApiException.badRequest("Actual qty < 0");
        it.setActualQuantity(actual);
        return itemDto(it);
    }

    @Transactional
    public InvoiceDto confirm(UUID id) {
        Invoice inv = invoices.findById(id)
                .orElseThrow(() -> ApiException.notFound("Invoice not found"));
        if (inv.getStatus() != Invoice.Status.DRAFT)
            throw ApiException.badRequest("Накладная уже подтверждена или отменена");

        List<InvoiceItem> lines = items.findByInvoiceId(id);
        if (lines.isEmpty())
            throw ApiException.badRequest("В накладной нет позиций");

        // Apply stock delta per item: +qty for INCOMING, -qty for OUTGOING.
        // Use actualQuantity if set, otherwise planned quantity.
        int sign = inv.getType() == Invoice.Type.INCOMING ? +1 : -1;

        for (InvoiceItem it : lines) {
            int qty = it.getActualQuantity() != null ? it.getActualQuantity() : it.getQuantity();
            if (qty <= 0) continue;
            int delta = sign * qty;

            Stock stock = stocks.findByProductAndCell(
                    it.getProduct().getId(), it.getCell().getId()).orElse(null);

            if (stock == null) {
                if (sign < 0)
                    throw ApiException.badRequest(
                            "Недостаточно остатка: "
                            + it.getProduct().getName()
                            + " в ячейке " + it.getCell().getCode());
                // create a fresh stock row for incoming
                stock = new Stock();
                stock.setProduct(it.getProduct());
                stock.setCell(it.getCell());
                stock.setQuantity(0);
            }
            int newQty = stock.getQuantity() + delta;
            if (newQty < 0)
                throw ApiException.badRequest(
                        "Недостаточно остатка: "
                        + it.getProduct().getName()
                        + " в ячейке " + it.getCell().getCode()
                        + " (есть " + stock.getQuantity() + ", надо " + qty + ")");
            stock.setQuantity(newQty);
            stock.setUpdatedAt(LocalDateTime.now());
            stocks.save(stock);
        }

        inv.setStatus(Invoice.Status.CONFIRMED);
        inv.setConfirmedAt(LocalDateTime.now());
        return toDto(inv);
    }

    private InvoiceDto toDto(Invoice i) {
        return new InvoiceDto(
                i.getId(), i.getInvoiceNumber(), i.getType().name(), i.getStatus().name(),
                i.getSupplier() == null ? null : i.getSupplier().getId(),
                i.getSupplier() == null ? null : i.getSupplier().getName(),
                i.getCreatedBy() == null ? null : i.getCreatedBy().getId(),
                i.getCreatedBy() == null ? null : i.getCreatedBy().getFullName(),
                i.getCreatedAt(), i.getConfirmedAt());
    }

    private InvoiceItemDto itemDto(InvoiceItem it) {
        return new InvoiceItemDto(
                it.getId(), it.getInvoice().getId(),
                it.getProduct().getId(), it.getProduct().getName(),
                it.getProduct().getSku(), it.getProduct().getBarcode(),
                it.getCell().getId(), it.getCell().getCode(),
                it.getQuantity(), it.getActualQuantity());
    }
}
