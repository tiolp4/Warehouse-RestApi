package ru.warehouse.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.warehouse.api.entity.InvoiceItem;

import java.util.List;
import java.util.UUID;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {
    List<InvoiceItem> findByInvoiceId(UUID invoiceId);
}
