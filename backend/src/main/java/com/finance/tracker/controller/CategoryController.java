package com.finance.tracker.controller;

import com.finance.tracker.dto.CategoryRequestDto;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    private final FinanceService financeService;

    public CategoryController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAll(@RequestParam(required = false) TransactionType type) {
        if (type != null) {
            return ResponseEntity.ok(financeService.getCategoryDao().findByType(type));
        }
        return ResponseEntity.ok(financeService.getCategoryDao().findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return financeService.getCategoryDao().findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequestDto dto) {
        Category created = financeService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequestDto dto) {
        Category updated = financeService.updateCategory(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        financeService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

