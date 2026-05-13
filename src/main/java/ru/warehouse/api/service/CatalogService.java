package ru.warehouse.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.warehouse.api.dto.Dtos.*;
import ru.warehouse.api.repository.*;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final SupplierRepository suppliers;
    private final ProductRepository products;
    private final StockRepository stocks;
    private final CellRepository cells;

    public CatalogService(SupplierRepository s, ProductRepository p,
                          StockRepository st, CellRepository c) {
        this.suppliers = s; this.products = p; this.stocks = st; this.cells = c;
    }

    public List<CellDto> listCells() {
        return cells.findAll().stream()
                .map(w -> new CellDto(w.getId(), w.getCode(), w.getZone(),
                        w.getRowNum(), w.getShelf(), w.getCapacity(), w.getIsOccupied()))
                .toList();
    }

    public List<SupplierDto> listSuppliers() {
        return suppliers.findAll().stream()
                .map(s -> new SupplierDto(s.getId(), s.getName(), s.getContact(), s.getPhone(), s.getEmail()))
                .toList();
    }

    public List<ProductDto> listProducts() {
        return products.findAll().stream()
                .map(p -> new ProductDto(p.getId(), p.getSku(), p.getName(), p.getBarcode(), p.getUnit()))
                .toList();
    }

    public List<StockDto> listStock() {
        return stocks.findAllPositive().stream()
                .map(s -> new StockDto(
                        s.getId(),
                        s.getProduct().getId(), s.getProduct().getName(), s.getProduct().getSku(),
                        s.getCell().getId(), s.getCell().getCode(), s.getCell().getZone(),
                        s.getQuantity()))
                .toList();
    }
}
