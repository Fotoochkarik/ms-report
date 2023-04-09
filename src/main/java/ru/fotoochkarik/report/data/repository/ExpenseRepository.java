package ru.fotoochkarik.report.data.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fotoochkarik.report.data.enums.ExpenseType;
import ru.fotoochkarik.report.data.model.ExpenseEntity;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, UUID> {

  @Query("select e from ExpenseEntity e where e.type= :type and month(e.effectiveDate) = :month and year(e.effectiveDate) = :year")
  Optional<ExpenseEntity> findByTypeAndMonthAndYear(@Param("type") ExpenseType type, @Param("month") Integer month, @Param("year") Integer year);

  List<ExpenseEntity> findByEffectiveDateBetween(ZonedDateTime startDate, ZonedDateTime endDate);

  @Query("select e from ExpenseEntity e where year(e.effectiveDate) = :year")
  List<ExpenseEntity> findAllByYear(@Param("year") Integer year);

}