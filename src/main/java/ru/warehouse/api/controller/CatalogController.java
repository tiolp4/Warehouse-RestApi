package ru.warehouse.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.warehouse.api.dto.Dtos.*;
import ru.warehouse.api.service.CatalogService;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class CatalogController {

    private final CatalogService svc;
    public CatalogController(CatalogService svc) { this.svc = svc; }

    @GetMapping("/suppliers")
    public List<SupplierDto> suppliers() { return svc.listSuppliers(); }

    @GetMapping("/products")
    public List<ProductDto> products() { return svc.listProducts(); }

    @GetMapping("/stock")
    public List<StockDto> stock() { return svc.listStock(); }

    @GetMapping("/cells")
    public List<CellDto> cells() { return svc.listCells(); }
}
