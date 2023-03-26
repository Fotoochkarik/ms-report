package ru.fotoochkarik.report.data.model;

import java.time.ZonedDateTime;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * @author v.schelkunov
 * @version 1.0
 * @since 1.0.0
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Table(schema = "report", name = "item")
public class Item extends BaseEntity {

  private Double nds;
  private Long sum;
  private String name;
  private Long price;
  private Double ndsSum;
  private Long quantity;
  private Integer paymentType;
  private Integer productType;
  private String propertiesItem;
  private Integer itemsQuantityMeasure;
  private ZonedDateTime payDate;

}
