package ru.warehouse.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.warehouse.api.entity.TransitShipment;

import java.util.List;
import java.util.UUID;

public interface TransitShipmentRepository extends JpaRepository<TransitShipment, UUID> {
    @Query("select t from TransitShipment t order by t.expectedArrival asc, t.createdAt desc")
    List<TransitShipment> findAllOrdered();
}
