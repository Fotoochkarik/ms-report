package ru.fotoochkarik.report.data.dto;

import java.time.Month;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.fotoochkarik.report.data.enums.ExpenseType;

@Getter
@Setter
@Builder
public class ExpenseResponse {

  private ExpenseType type;
  private double sum;
  private Double totalSum;
  private Month month;
  private Integer year;

}
