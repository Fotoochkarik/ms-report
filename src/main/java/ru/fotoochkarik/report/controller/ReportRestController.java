package ru.fotoochkarik.report.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.fotoochkarik.report.data.dto.ExpenseRequest;
import ru.fotoochkarik.report.data.dto.ExpenseResponse;
import ru.fotoochkarik.report.service.ReportService;

@RestController
@RequiredArgsConstructor
public class ReportRestController {

  private final ReportService reportService;

  @GetMapping("/report")
  public ResponseEntity<Void> getReportWithPivotTable() throws IOException {
    reportService.createReportWithPivotTable();
    return ResponseEntity.ok().build();
  }

  @GetMapping("/excel")
  public void generateExcelReport(HttpServletResponse response) throws IOException {
    response.setContentType("application/octet-stream");
    String headerKey = "Content-Disposition";
    String headerValue = "attachment;filename=ReportExcel.xls";
    response.setHeader(headerKey, headerValue);
    reportService.createWebReport(response);
  }

  @PostMapping("/add")
  public ResponseEntity<ExpenseResponse> add(@RequestBody ExpenseRequest expenseRequest) {
    return ResponseEntity.ok(reportService.add(expenseRequest));
  }

  @GetMapping("/report-year")
  public ResponseEntity<Void> getReportYear() throws IOException {
    reportService.createReportYear();
    return ResponseEntity.ok().build();
  }

}
