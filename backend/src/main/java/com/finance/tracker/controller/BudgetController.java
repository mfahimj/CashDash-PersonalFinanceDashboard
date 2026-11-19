package com.finance.tracker.controller;

import com.finance.tracker.dto.BudgetRequestDto;
import com.finance.tracker.model.Budget;
import com.finance.tracker.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "*")
public class BudgetController {
    private final FinanceService financeService;

    public BudgetController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getBudgets(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(financeService.getBudgetsWithCalculations(m, y));
    }

    @PostMapping
    public ResponseEntity<Budget> create(@Valid @RequestBody BudgetRequestDto dto) {
        Budget created = financeService.createBudget(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Budget> update(@PathVariable Long id, @Valid @RequestBody BudgetRequestDto dto) {
        Budget updated = financeService.updateBudget(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = financeService.deleteBudget(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

