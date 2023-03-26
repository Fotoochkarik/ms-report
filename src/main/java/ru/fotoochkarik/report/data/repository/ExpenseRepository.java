package ru.fotoochkarik.report.data.repository;

import java.time.Month;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fotoochkarik.report.data.enums.ExpenseType;
import ru.fotoochkarik.report.data.model.ExpenseEntity;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, UUID> {

  Optional<ExpenseEntity> findByTypeAndMonth(ExpenseType type, Month month);

}