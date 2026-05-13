package ru.warehouse.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.warehouse.api.entity.WarehouseCell;

import java.util.UUID;

public interface CellRepository extends JpaRepository<WarehouseCell, UUID> {}
