package ru.warehouse.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.warehouse.api.entity.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySku(String sku);
    Optional<Product> findByBarcode(String barcode);
}
