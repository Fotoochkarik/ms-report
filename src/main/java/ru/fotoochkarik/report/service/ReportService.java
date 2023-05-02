package ru.fotoochkarik.report.service;

import static java.util.Objects.nonNull;
import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
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
import ru.fotoochkarik.report.data.repository.ExpenseRepository;

/**
 * @author v.schelkunov
 * @version 1.0
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

  private static final int FIRST_NUM_OF_COLUMNS = 0;
  private static final int ZERO = 0;
  private static final int LAST_NUM_OF_COLUMNS = 13;
  private static final int DEFAULT_COLUMN_WIDTH = 3500;
  private static final int FIRST_COLUMN_DRAWING_PATRIARCH = 0;
  private static final int FIRST_ROW_DRAWING_PATRIARCH = 15;
  private static final int LAST_COLUMN_DRAWING_PATRIARCH = 14;
  private static final int LAST_ROW_DRAWING_PATRIARCH = 35;
  private final ExpenseRepository expenseRepository;

  @Transactional
  public ExpenseResponse add(ExpenseRequest expenseRequest) {
    log.info("add request = {}", expenseRequest);
    var type = expenseRequest.getType();
    int month = expenseRequest.getPayDate().getMonth().getValue();
    int year = expenseRequest.getPayDate().getYear();
    var requestSum = nonNull(expenseRequest.getSum()) ? expenseRequest.getSum() : 0;
    var expenseOptional = expenseRepository.findByTypeAndMonthAndYear(type, month, year);
    ExpenseEntity expense;
    if (expenseOptional.isPresent()) {
      expense = expenseOptional.get();
      var sum = nonNull(expense.getSum()) ? expense.getSum() : 0;
      expense.setSum(sum + requestSum);
    } else {
      expense = new ExpenseEntity();
      expense.setType(expenseRequest.getType());
      expense.setSum(requestSum);
    }
    expense.setPayDate(expenseRequest.getPayDate());
    expense.setEffectiveDate(ZonedDateTime.now());
    var saved = expenseRepository.save(expense);
    return ExpenseResponse.builder()
        .type(saved.getType())
        .month(saved.getPayDate().getMonth())
        .sum(requestSum)
        .totalSum(saved.getSum())
        .year(saved.getPayDate().getYear())
        .build();
  }

  public void createReportPeriod(LocalDate startDate, LocalDate endDate) throws IOException {
    var expenseList = expenseRepository.findByPayDateBetween(startDate, endDate);
    createReportYear(expenseList);
  }

  public void createReportYear(Integer year) throws IOException {
    var expenseList = expenseRepository.findAllByYear(year);
    createReportYear(expenseList);
  }

  public void createReportYear(List<ExpenseEntity> expenseList) throws IOException {
    var workbook = getWorkbook(expenseList);
    writeFile(workbook);
    workbook.close();
  }

  public void downloadReport(Integer year, HttpServletResponse response) throws IOException {
    var expenseList = expenseRepository.findAllByYear(year);
    getReportYear(expenseList, response);
  }

  private void getReportYear(List<ExpenseEntity> expenseList, HttpServletResponse response) throws IOException {
    var workbook = getWorkbook(expenseList);
    var responseOutputStream = response.getOutputStream();
    workbook.write(responseOutputStream);
    workbook.close();
  }

  private XSSFWorkbook getWorkbook(List<ExpenseEntity> expenseList) {
    var workbook = new XSSFWorkbook();
    var reportSheet = workbook.createSheet("Report");
    var chart = addChart(reportSheet);
    var scaleX = createScaleX(reportSheet);
    var data = getLineChartData(chart);
    var rowIndex = new AtomicInteger(0);
    createHeaderTable(workbook, reportSheet.createRow(rowIndex.getAndIncrement()));
    createCountingRow(workbook, reportSheet, rowIndex, scaleX, data);
    fillTable(workbook, reportSheet, scaleX, data, rowIndex, expenseList);
    addSizeColumn(reportSheet);
    chart.plot(data);
    return workbook;
  }

  private XDDFLineChartData getLineChartData(XSSFChart chart) {
    var bottomAxis = addDateAxis(chart);
    var leftAxis = addValueAxis(chart);
    return (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
  }

  private void writeFile(XSSFWorkbook workbook) throws IOException {
    var fileOutputStream = new FileOutputStream("ReportYearExcel.xlsx");
    workbook.write(fileOutputStream);
    fileOutputStream.close();
  }

  private XDDFCategoryDataSource createScaleX(XSSFSheet reportSheet) {
    return XDDFDataSourcesFactory.fromStringCellRange(reportSheet, new CellRangeAddress(ZERO, ZERO, FIRST_NUM_OF_COLUMNS + 1, LAST_NUM_OF_COLUMNS - 1));
  }

  private XSSFClientAnchor createAnchor(XSSFDrawing drawing) {
    return drawing.createAnchor(ZERO, ZERO, ZERO, ZERO, FIRST_COLUMN_DRAWING_PATRIARCH, FIRST_ROW_DRAWING_PATRIARCH, LAST_COLUMN_DRAWING_PATRIARCH,
        LAST_ROW_DRAWING_PATRIARCH);
  }

  private void createHeaderTable(XSSFWorkbook workbook, Row rowHeader) {
    EnumSet<Month> months = EnumSet.allOf(Month.class);
    var cellStyle = createCellStyle(workbook, IndexedColors.GREY_25_PERCENT.getIndex(), false);
    months.forEach(month -> {
          var cell = rowHeader.createCell(month.getValue());
          cell.setCellStyle(cellStyle);
          cell.setCellValue(String.valueOf(month));
        }
    );
    var cell = rowHeader.createCell(LAST_NUM_OF_COLUMNS);
    cell.setCellValue("Итого:");
    cell.setCellStyle(cellStyle);
  }

  private void fillTable(XSSFWorkbook workbook, XSSFSheet reportSheet, XDDFCategoryDataSource scaleX, XDDFLineChartData data, AtomicInteger rowIndex,
      List<ExpenseEntity> expenseList) {
    getExpenseMap(expenseList).forEach((key, listEntryValue) -> {
          var cellStyle = createCellStyle(workbook, IndexedColors.WHITE1.getIndex(), false);
          var dataRow = reportSheet.createRow(rowIndex.get());
          var cell = dataRow.createCell(FIRST_NUM_OF_COLUMNS);
          cell.setCellStyle(cellStyle);
          cell.setCellValue(String.valueOf(key));
          listEntryValue.forEach(expense -> {
            var rowCell = dataRow.createCell(expense.getPayDate().getMonth().getValue());
            rowCell.setCellValue(expense.getSum());
            rowCell.setCellStyle(cellStyle);
          });
          dataRow.createCell(LAST_NUM_OF_COLUMNS).setCellFormula(String.format("SUM(B%s:M%s)", rowIndex, rowIndex));
          createRange(reportSheet, scaleX, data, rowIndex.getAndIncrement(), String.valueOf(key), MarkerStyle.CIRCLE);
        }
    );
  }

  private XSSFChart addChart(XSSFSheet reportSheet) {
    var drawing = reportSheet.createDrawingPatriarch();
    var anchor = createAnchor(drawing);
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

  private Map<ExpenseType, List<ExpenseEntity>> getExpenseMap(List<ExpenseEntity> expenseList) {
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
    font.setFontHeightInPoints(XSSFFont.DEFAULT_FONT_SIZE);
    font.setBold(bold);
    return font;
  }

  private void addAllBorder(XSSFCellStyle cellStyle) {
    cellStyle.setBorderBottom(BorderStyle.THIN);
    cellStyle.setBorderLeft(BorderStyle.THIN);
    cellStyle.setBorderRight(BorderStyle.THIN);
    cellStyle.setBorderTop(BorderStyle.THIN);
  }

  private void createCountingRow(XSSFWorkbook workbook, XSSFSheet reportSheet, AtomicInteger indexRow, XDDFCategoryDataSource scaleX, XDDFLineChartData data) {
    var cellStyle = createCellStyle(workbook, IndexedColors.WHITE1.getIndex(), true);
    var countingRow = reportSheet.createRow(indexRow.get());
    var cellIndex = new AtomicInteger();
    var cell = countingRow.createCell(cellIndex.getAndIncrement());
    cell.setCellValue("Итого:");
    cell.setCellStyle(cellStyle);
    var literalList = List.of("B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N");
    literalList.forEach(literal -> {
      var rowCell = countingRow.createCell(cellIndex.getAndIncrement());
      rowCell.setCellFormula(String.format("SUM(%s3:%s6)", literal, literal));
      rowCell.setCellStyle(cellStyle);
    });
    createCountingRange(reportSheet, scaleX, data, indexRow.getAndIncrement());
  }

  private void createRange(XSSFSheet reportSheet, XDDFCategoryDataSource xs, XDDFLineChartData data, int indexRow, String title, MarkerStyle markerStyle) {
    XDDFNumericalDataSource<Double> scaleY =
        XDDFDataSourcesFactory.fromNumericCellRange(reportSheet, new CellRangeAddress(indexRow, indexRow, FIRST_NUM_OF_COLUMNS + 1, LAST_NUM_OF_COLUMNS - 1));
    var series = (Series) data.addSeries(xs, scaleY);
    series.setTitle(title, null);
    series.setSmooth(false);
    series.setMarkerStyle(markerStyle);
  }

  private void createCountingRange(XSSFSheet reportSheet, XDDFCategoryDataSource scaleX, XDDFLineChartData data, int indexRow) {
    createRange(reportSheet, scaleX, data, indexRow, "Итого", MarkerStyle.STAR);
  }

  private void addSizeColumn(XSSFSheet sheet) {
    sheet.autoSizeColumn(FIRST_NUM_OF_COLUMNS);
    for (int i = 1; i < LAST_NUM_OF_COLUMNS + 1; i++) {
      sheet.setColumnWidth(i, DEFAULT_COLUMN_WIDTH);
    }
  }

}
