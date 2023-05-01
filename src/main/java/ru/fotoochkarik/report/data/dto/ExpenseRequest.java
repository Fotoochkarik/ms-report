package ru.fotoochkarik.report.data.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.fotoochkarik.report.data.enums.ExpenseType;

@Getter
@Setter
@Builder
@ToString
public class ExpenseRequest {

  private ExpenseType type;
  private Double sum;
  private LocalDate payDate;

}
