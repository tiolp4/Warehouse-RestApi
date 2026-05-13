package ru.warehouse.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.warehouse.api.entity.Supplier;

import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {}
