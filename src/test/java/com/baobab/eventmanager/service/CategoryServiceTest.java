package com.baobab.eventmanager.service;

import com.baobab.eventmanager.model.Category;
import com.baobab.eventmanager.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void addCategory_ShouldSaveCategory_WhenParentIsNull() {
        Category category = new Category("Test");
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.addCategory("Test", null);

        assertNotNull(result);
        assertEquals("Test", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void addCategory_ShouldSaveCategory_WhenParentIsValid() {
        Category parent = new Category("Parent");
        parent.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));

        Category child = new Category("Child");
        child.setParent(parent);
        when(categoryRepository.save(any(Category.class))).thenReturn(child);

        Category result = categoryService.addCategory("Child", 1L);

        assertNotNull(result);
        assertEquals(parent, result.getParent());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void addCategory_ShouldThrowException_WhenParentNotFound() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> categoryService.addCategory("Child", 99L));
    }

    @Test
    void removeCategory_ShouldDelete_WhenFound() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        categoryService.removeCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void removeCategory_ShouldThrowException_WhenNotFound() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> categoryService.removeCategory(1L));
    }

    @Test
    void getCategorySubtree_ShouldReturnCategory_WhenFound() {
        Category category = new Category("Root");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategorySubtree(1L);

        assertEquals("Root", result.getName());
    }

    @Test
    void moveSubtree_ShouldUpdateParent_WhenMoveIsValid() {
        Category child = new Category("Child");
        child.setId(2L);

        Category newParent = new Category("NewParent");
        newParent.setId(1L);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(child));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(newParent));
        when(categoryRepository.save(child)).thenReturn(child);

        Category result = categoryService.moveSubtree(2L, 1L);

        assertEquals(newParent, result.getParent());
    }

    @Test
    void moveSubtree_ShouldThrowException_WhenCycleDetected() {
        // Setup a cycle: moving A to be a child of B, where B is a child of A
        Category parentA = new Category("A");
        parentA.setId(1L);

        Category childB = new Category("B");
        childB.setId(2L);

        // Verify that moving a node to its own descendant is invalid
        childB.setParent(parentA);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentA));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(childB));

        assertThrows(IllegalArgumentException.class, () -> categoryService.moveSubtree(1L, 2L));
    }

    @Test
    void getCategorySubtree_ShouldThrowException_WhenNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> categoryService.getCategorySubtree(1L));
    }

    @Test
    void moveSubtree_ShouldMoveToRoot_WhenNewParentIsNull() {
        Category category = new Category("Node");
        category.setId(1L);
        category.setParent(new Category("OldParent"));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);

        Category result = categoryService.moveSubtree(1L, null);

        assertNull(result.getParent());
    }

    @Test
    void moveSubtree_ShouldDoNothing_WhenNewParentIsSameAsOld() {
        Category parent = new Category("Parent");
        parent.setId(2L);

        Category child = new Category("Child");
        child.setId(1L);
        child.setParent(parent);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(child));

        Category result = categoryService.moveSubtree(1L, 2L);

        assertEquals(parent, result.getParent());
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
