package ru.fotoochkarik.report.data.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.fotoochkarik.report.data.enums.ExpenseType;

/**
 * @author v.schelkunov
 * @version 1.0
 * @since 1.0.0
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(schema = "report", name = "expense")
@EqualsAndHashCode(callSuper = true)
public class ExpenseEntity extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ExpenseType type;

  @Column(name = "sum")
  private Double sum;

  @Column(name = "pay_date")
  private LocalDate payDate;

  @Column(name = "effective_date")
  private ZonedDateTime effectiveDate;

}
