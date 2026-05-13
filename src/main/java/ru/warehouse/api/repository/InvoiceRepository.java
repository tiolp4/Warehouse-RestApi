package ru.warehouse.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.warehouse.api.entity.Invoice;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    @Query("select i from Invoice i order by i.createdAt desc")
    List<Invoice> findAllOrdered();
}
