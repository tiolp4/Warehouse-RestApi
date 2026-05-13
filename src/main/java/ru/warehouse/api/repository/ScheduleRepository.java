package ru.warehouse.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.warehouse.api.entity.LogisticsSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<LogisticsSchedule, UUID> {
    @Query("select s from LogisticsSchedule s where s.workDate between ?1 and ?2 order by s.workDate, s.shiftStart")
    List<LogisticsSchedule> findInRange(LocalDate from, LocalDate to);

    @Query("select s from LogisticsSchedule s order by s.workDate desc, s.shiftStart asc")
    List<LogisticsSchedule> findAllOrdered();
}
