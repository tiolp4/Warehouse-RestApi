package ru.warehouse.api.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "warehouse_cells")
public class WarehouseCell {
    @Id @GeneratedValue
    private UUID id;
    @Column(unique = true, nullable = false)
    private String code;
    private String zone;
    @Column(name = "row_num") private Integer rowNum;
    private Integer shelf;
    private Integer capacity;
    @Column(name = "is_occupied") private Boolean isOccupied;

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getZone() { return zone; }
    public Integer getRowNum() { return rowNum; }
    public Integer getShelf() { return shelf; }
    public Integer getCapacity() { return capacity; }
    public Boolean getIsOccupied() { return isOccupied; }
}
