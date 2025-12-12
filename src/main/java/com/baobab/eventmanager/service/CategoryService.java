package com.baobab.eventmanager.service;

import com.baobab.eventmanager.model.Category;
import com.baobab.eventmanager.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category addCategory(String name, Long parentId) {
        Category category = new Category(name);
        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found with id: " + parentId));
            category.setParent(parent);
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void removeCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Category getCategorySubtree(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    @Transactional
    public Category moveSubtree(Long id, Long newParentId) {
        Category nodeToMove = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));

        // Moving to root
        if (newParentId == null) {
            nodeToMove.setParent(null);
            return categoryRepository.save(nodeToMove);
        }

        // Optimization: No need to update if parent hasn't changed
        if (nodeToMove.getParent() != null && nodeToMove.getParent().getId().equals(newParentId)) {
            return nodeToMove;
        }

        Category newParent = categoryRepository.findById(newParentId)
                .orElseThrow(
                        () -> new IllegalArgumentException("New parent category not found with id: " + newParentId));

        validateMove(nodeToMove, newParent);

        nodeToMove.setParent(newParent);
        return categoryRepository.save(nodeToMove);
    }

    private void validateMove(Category nodeToMove, Category newParent) {
        // Prevent cycles: cannot move a node into its own descendant
        Category current = newParent;
        while (current != null) {
            if (current.getId().equals(nodeToMove.getId())) {
                throw new IllegalArgumentException("Cannot move category into its own subtree. Cycle detected.");
            }
            current = current.getParent();
        }
    }
}
