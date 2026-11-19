package com.finance.tracker.controller;

import com.finance.tracker.model.DashboardSummary;
import com.finance.tracker.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    private final FinanceService financeService;

    public DashboardController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getSummary(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        DashboardSummary summary = financeService.getDashboardSummary(m, y);
        return ResponseEntity.ok(summary);
    }
}

