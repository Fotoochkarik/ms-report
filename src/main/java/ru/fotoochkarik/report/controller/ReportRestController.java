package ru.fotoochkarik.report.controller;

import java.io.IOException;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
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
@RequestMapping("/v1")
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
  public ResponseEntity<Void> getReportPeriod(@RequestParam("startDate") @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime startDate,
      @RequestParam("endDate") @DateTimeFormat(iso = ISO.DATE_TIME) ZonedDateTime endDate) throws IOException {
    reportService.createReportPeriod(startDate, endDate);
    return ResponseEntity.ok().build();
  }

}
