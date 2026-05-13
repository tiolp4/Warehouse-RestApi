package ru.warehouse.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.warehouse.api.entity.Stock;

import java.util.List;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    @Query("select s from Stock s where s.quantity > 0 order by s.product.name")
    List<Stock> findAllPositive();

    @Query("select s from Stock s where s.product.id = ?1 and s.cell.id = ?2")
    java.util.Optional<Stock> findByProductAndCell(UUID productId, UUID cellId);
}
