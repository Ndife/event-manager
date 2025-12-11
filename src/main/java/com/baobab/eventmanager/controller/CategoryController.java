package com.baobab.eventmanager.controller;

import com.baobab.eventmanager.dto.CreateCategoryRequest;
import com.baobab.eventmanager.model.Category;
import com.baobab.eventmanager.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Category> addCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Category created = categoryService.addCategory(request.getName(), request.getParentId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeCategory(@PathVariable Long id) {
        categoryService.removeCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategorySubtree(@PathVariable Long id) {
        Category category = categoryService.getCategorySubtree(id);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<Category> moveSubtree(@PathVariable Long id, @RequestParam(required = false) Long newParentId) {
        Category moved = categoryService.moveSubtree(id, newParentId);
        return ResponseEntity.ok(moved);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
