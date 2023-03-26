package ru.fotoochkarik.report.data.dto;

import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.fotoochkarik.report.data.enums.ExpenseType;

@Getter
@Setter
@Builder
public class ExpenseRequest {

  private ExpenseType type;
  private Long sum;
  private ZonedDateTime payDate;

}
