package ru.fotoochkarik.report.data.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fotoochkarik.report.data.enums.ExpenseType;
import ru.fotoochkarik.report.data.model.ExpenseEntity;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, UUID> {

  @Query("select e from ExpenseEntity e where e.type= :type and month(e.payDate) = :month and year(e.payDate) = :year")
  Optional<ExpenseEntity> findByTypeAndMonthAndYear(@Param("type") ExpenseType type, @Param("month") Integer month, @Param("year") Integer year);

  List<ExpenseEntity> findByPayDateBetween(LocalDate startDate, LocalDate endDate);

  @Query("select e from ExpenseEntity e where year(e.payDate) = :year")
  List<ExpenseEntity> findAllByYear(@Param("year") Integer year);

}