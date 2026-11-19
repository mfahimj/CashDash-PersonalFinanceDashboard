package com.finance.tracker.controller;

import com.finance.tracker.model.MonthlyReportItem;
import com.finance.tracker.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    private final FinanceService financeService;

    public AnalyticsController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyReportItem>> getMonthlyTrends(
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(financeService.getMonthlyTrends(months));
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryBreakdown(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(financeService.getCategoryBreakdown(m, y));
    }
}

