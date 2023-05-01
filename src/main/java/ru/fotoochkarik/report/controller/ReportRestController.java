package ru.fotoochkarik.report.controller;

import java.io.IOException;
import java.time.LocalDate;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.fotoochkarik.report.data.dto.ExpenseRequest;
import ru.fotoochkarik.report.data.dto.ExpenseResponse;
import ru.fotoochkarik.report.service.ReportService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReportRestController {

  private final ReportService reportService;

  @PostMapping("/add")
  public ResponseEntity<ExpenseResponse> add(@RequestBody ExpenseRequest expenseRequest) {
    return ResponseEntity.ok(reportService.add(expenseRequest));
  }

  @GetMapping("/report-year")
  public ResponseEntity<Void> getReportYear(@RequestParam("year") Integer year) throws IOException {
    reportService.createReportYear(year);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/period")
  public ResponseEntity<Void> getReportPeriod(@RequestParam("startDate") @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = ISO.DATE) LocalDate endDate) throws IOException {
    reportService.createReportPeriod(startDate, endDate);
    return ResponseEntity.ok().build();
  }

  @GetMapping(value = "/download-report")
  public void downloadReport(@RequestParam("year") Integer year, HttpServletResponse response) throws IOException {
    response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=ReportExcel-%s.xlsx", year));
    reportService.downloadReport(year, response);
  }

}
