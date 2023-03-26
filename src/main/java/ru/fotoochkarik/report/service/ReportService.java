package ru.fotoochkarik.report.service;

import static java.util.Objects.nonNull;
import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFDateAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData.Series;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fotoochkarik.report.data.dto.ExpenseRequest;
import ru.fotoochkarik.report.data.dto.ExpenseResponse;
import ru.fotoochkarik.report.data.enums.ExpenseType;
import ru.fotoochkarik.report.data.model.ExpenseEntity;
import ru.fotoochkarik.report.data.model.Item;
import ru.fotoochkarik.report.data.repository.ExpenseRepository;
import ru.fotoochkarik.report.data.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ItemRepository itemRepository;
  private final ExpenseRepository expenseRepository;
  private static final Integer NUM_OF_COLUMNS = 13;
  private final static Integer FIRST_NUM_OF_COLUMNS = 0;

  public void createReportWithPivotTable() throws IOException {
    var itemList = itemRepository.findAll();
    var workbook = new XSSFWorkbook();
    var reportSheet = workbook.createSheet("Report");
    var headerRow = reportSheet.createRow(0);
    createHeaderTableTest(headerRow);
    addItemToTable(itemList, reportSheet);

    var endPivotTable = itemList.size();

    var areaReference = new AreaReference(String.format("A1:D%s", endPivotTable), SpreadsheetVersion.EXCEL2007);
    var cellReference = new CellReference("H5");
    var pivotTable = reportSheet.createPivotTable(areaReference, cellReference);

    pivotTable.addRowLabel(0);

    pivotTable.addColumnLabel(DataConsolidateFunction.SUM, 2);
    pivotTable.addColumnLabel(DataConsolidateFunction.SUM, 3, "SUM");

    var fileOutputStream = new FileOutputStream("ReportExcel.xlsx");
    workbook.write(fileOutputStream);

    fileOutputStream.close();
    workbook.close();
  }

  private void createHeaderTableTest(Row headerRow) {
    headerRow.createCell(0).setCellValue("Name");
    headerRow.createCell(1).setCellValue("Quantity");
    headerRow.createCell(2).setCellValue("Price");
    headerRow.createCell(3).setCellValue("Sum");
    headerRow.createCell(4).setCellValue("Pay date");
  }

  public void createWebReport(HttpServletResponse response) throws IOException {
    var itemList = itemRepository.findAll();
    var workbook = new HSSFWorkbook();
    var reportSheet = workbook.createSheet("Report");
    var headerRow = reportSheet.createRow(0);
    createHeaderTableTest(headerRow);
    addItemToTable(itemList, reportSheet);
    var responseOutputStream = response.getOutputStream();
    workbook.write(responseOutputStream);
    workbook.close();
    responseOutputStream.close();
  }

  private void addItemToTable(List<Item> itemList, Sheet reportSheet) {
    int dataRowIndex = 1;
    for (Item item : itemList) {
      var dataRow = reportSheet.createRow(dataRowIndex++);
      dataRow.createCell(0).setCellValue(item.getName());
      dataRow.createCell(1).setCellValue(item.getQuantity());
      dataRow.createCell(2).setCellValue(item.getPrice());
      dataRow.createCell(3).setCellValue(item.getSum());
      dataRow.createCell(4).setCellValue(item.getPayDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
  }

  @Transactional
  public ExpenseResponse add(ExpenseRequest expenseRequest) {
    var expenseOptional = expenseRepository.findByTypeAndMonth(expenseRequest.getType(), expenseRequest.getPayDate().getMonth());
    if (expenseOptional.isPresent()) {
      Long sum = expenseOptional.get().getSum();
      var expense = expenseOptional.get();
      expense.setSum(nonNull(sum) ? sum + expenseRequest.getSum() : expenseRequest.getSum()
      );
      expense.setEffectiveDate(expenseRequest.getPayDate());
      return ExpenseResponse.builder()
          .type(expense.getType())
          .month(expense.getMonth())
          .sum(expense.getSum())
          .build();
    } else {
      var expenseEntity = new ExpenseEntity();
      expenseEntity.setType(expenseRequest.getType());
      expenseEntity.setMonth(expenseRequest.getPayDate().getMonth());
      expenseEntity.setSum(expenseRequest.getSum());
      expenseEntity.setEffectiveDate(expenseRequest.getPayDate());
      var save = expenseRepository.save(expenseEntity);
      return ExpenseResponse.builder()
          .type(save.getType())
          .month(save.getMonth())
          .sum(save.getSum())
          .build();
    }
  }

  public void createReportYear() throws IOException {
    var workbook = new XSSFWorkbook();
    var reportSheet = workbook.createSheet("Report");

    var drawing = reportSheet.createDrawingPatriarch();
    var anchor = drawing.createAnchor(0, 0, 0, 0, 0, 15, 14, 35);

    var chart = addChart(drawing, anchor);
    var bottomAxis = addDateAxis(chart);
    var leftAxis = addValueAxis(chart);

    var xs = XDDFDataSourcesFactory.fromStringCellRange(reportSheet, new CellRangeAddress(0, 0, 1, NUM_OF_COLUMNS - 1));
    var data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

    var rowIndex = new AtomicInteger(0);
    createHeaderTable(workbook, reportSheet.createRow(rowIndex.getAndIncrement()));

    fillTable(workbook, reportSheet, xs, data, rowIndex);

    createFinalRow(workbook, reportSheet, rowIndex.get());
    createFinalRange(reportSheet, xs, data);
    addSizeColumn(reportSheet);
    chart.plot(data);

    var fileOutputStream = new FileOutputStream("ReportYearExcel.xlsx");
    workbook.write(fileOutputStream);

    fileOutputStream.close();
    workbook.close();
  }

  private void createHeaderTable(XSSFWorkbook workbook, Row rowHeader) {
    EnumSet<Month> months = EnumSet.allOf(Month.class);
    AtomicInteger index = new AtomicInteger(1);
    var cellStyle = createCellStyle(workbook, IndexedColors.GREY_25_PERCENT.getIndex(), false);
    months.forEach(month -> {
          var cell = rowHeader.createCell(index.getAndIncrement());
          cell.setCellStyle(cellStyle);
          cell.setCellValue(String.valueOf(month));
        }
    );
    var cell = rowHeader.createCell(index.get());
    cell.setCellValue("Итого");
    cell.setCellStyle(cellStyle);
  }

  private void fillTable(XSSFWorkbook workbook, XSSFSheet reportSheet, XDDFCategoryDataSource xs, XDDFLineChartData data, AtomicInteger rowIndex) {
    getExpenseMap().forEach((key, listEntryValue) -> {
          var cellStyle = createCellStyle(workbook, IndexedColors.WHITE1.getIndex(), false);
          var dataRow = reportSheet.createRow(rowIndex.getAndIncrement());
          var cell = dataRow.createCell(FIRST_NUM_OF_COLUMNS);
          cell.setCellStyle(cellStyle);
          cell.setCellValue(String.valueOf(key));
          var cellIndex = new AtomicInteger(1);
          listEntryValue.forEach(expense -> {
            var rowCell = dataRow.createCell(cellIndex.getAndIncrement());
            rowCell.setCellValue(expense.getSum());
            rowCell.setCellStyle(cellStyle);
          });
          dataRow.createCell(NUM_OF_COLUMNS).setCellFormula(String.format("SUM(B%s:M%s)", rowIndex, rowIndex));
          createRange(reportSheet, xs, data, rowIndex.get(), String.valueOf(key), MarkerStyle.CIRCLE);
        }
    );
  }

  private XSSFChart addChart(XSSFDrawing drawing, XSSFClientAnchor anchor) {
    var chart = drawing.createChart(anchor);
    var legend = chart.getOrAddLegend();
    legend.setPosition(LegendPosition.TOP_RIGHT);
    return chart;
  }

  private XDDFValueAxis addValueAxis(XSSFChart chart) {
    var leftAxis = chart.createValueAxis(AxisPosition.LEFT);
    leftAxis.setTitle("Rubles");
    leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
    return leftAxis;
  }

  private XDDFDateAxis addDateAxis(XSSFChart chart) {
    var bottomAxis = chart.createDateAxis(AxisPosition.BOTTOM);
    bottomAxis.setTitle("Month");
    return bottomAxis;
  }

  private Map<ExpenseType, List<ExpenseEntity>> getExpenseMap() {
    var expenseList = expenseRepository.findAll();
    return expenseList.stream()
        .collect(Collectors.groupingBy(ExpenseEntity::getType));
  }

  private XSSFCellStyle createCellStyle(XSSFWorkbook workbook, Short fillColor, Boolean bold) {
    var cellStyle = workbook.createCellStyle();
    addStyleFields(cellStyle, fillColor);
    addAllBorder(cellStyle);
    var font = createFont(workbook, bold);
    cellStyle.setFont(font);
    return cellStyle;
  }

  private void addStyleFields(XSSFCellStyle cellStyle, Short fillColor) {
    cellStyle.setFillPattern(SOLID_FOREGROUND);
    cellStyle.setFillForegroundColor(fillColor);
    cellStyle.setAlignment(HorizontalAlignment.CENTER);
    cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
  }

  private XSSFFont createFont(XSSFWorkbook workbook, Boolean bold) {
    var font = workbook.createFont();
    font.setFontName("Times New Roman");
    font.setColor(IndexedColors.BLACK1.getIndex());
    font.setFontHeightInPoints((short) 11);
    font.setBold(bold);
    return font;
  }

  private void addAllBorder(XSSFCellStyle cellStyle) {
    cellStyle.setBorderBottom(BorderStyle.THIN);
    cellStyle.setBorderLeft(BorderStyle.THIN);
    cellStyle.setBorderRight(BorderStyle.THIN);
    cellStyle.setBorderTop(BorderStyle.THIN);
  }

  private void createFinalRow(XSSFWorkbook workbook, XSSFSheet reportSheet, int indexRow) {
    var cellStyle = createCellStyle(workbook, IndexedColors.WHITE1.getIndex(), true);
    var finalRow = reportSheet.createRow(indexRow);
    var cellIndex = new AtomicInteger();
    var cell = finalRow.createCell(cellIndex.getAndIncrement());
    cell.setCellValue("Итого:");
    cell.setCellStyle(cellStyle);
    var literalList = List.of("B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N");
    literalList.forEach(literal -> {
      var rowCell = finalRow.createCell(cellIndex.getAndIncrement());
      rowCell.setCellFormula(String.format("SUM(%s2:%s5)", literal, literal));
      rowCell.setCellStyle(cellStyle);
    });
  }

  private void createRange(XSSFSheet reportSheet, XDDFCategoryDataSource xs, XDDFLineChartData data, int indexRow, String title, MarkerStyle markerStyle) {
    XDDFNumericalDataSource<Double> ys =
        XDDFDataSourcesFactory.fromNumericCellRange(reportSheet, new CellRangeAddress(indexRow, indexRow, FIRST_NUM_OF_COLUMNS + 1, NUM_OF_COLUMNS - 1));
    Series series1 = (Series) data.addSeries(xs, ys);
    series1.setTitle(title, null);
    series1.setSmooth(false);
    series1.setMarkerStyle(markerStyle);
  }

  private void createFinalRange(XSSFSheet reportSheet, XDDFCategoryDataSource xs, XDDFLineChartData data) {
    createRange(reportSheet, xs, data, 5, "Итого", MarkerStyle.STAR);
  }

  private void addSizeColumn(XSSFSheet sheet) {
    sheet.autoSizeColumn(FIRST_NUM_OF_COLUMNS);
    for (int i = 1; i < NUM_OF_COLUMNS + 1; i++) {
      sheet.setColumnWidth(i, 3500);
    }
  }

}
