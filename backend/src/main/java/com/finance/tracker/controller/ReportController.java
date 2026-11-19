package com.finance.tracker.controller;

import com.finance.tracker.model.Transaction;
import com.finance.tracker.service.FinanceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    private final FinanceService financeService;

    public ReportController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportCsv() {
        List<Transaction> list = financeService.getTransactionDao().findAllWithFilters(null, null, null, null, null, null);
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction ID,Description,Date,Category,Type,Payment Method,Amount,Notes\n");
        for (Transaction tx : list) {
            sb.append(tx.getId()).append(",")
              .append("\"").append(tx.getDescription().replace("\"", "\"\"")).append("\",")
              .append(tx.getTransactionDate()).append(",")
              .append("\"").append(tx.getCategoryName() != null ? tx.getCategoryName() : "Other").append("\",")
              .append(tx.getType().name()).append(",")
              .append("\"").append(tx.getPaymentMethod()).append("\",")
              .append(tx.getAmount().toPlainString()).append(",")
              .append("\"").append(tx.getNotes() != null ? tx.getNotes().replace("\"", "\"\"") : "").append("\"\n");
        }

        byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"finance_report_" + LocalDate.now() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }
}

