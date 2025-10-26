package com.project.demo.rest.category;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USER')")
    public ResponseEntity<?> getAllCategories(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           HttpServletRequest request){

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoryPage.getTotalPages());
        meta.setTotalElements(categoryPage.getTotalElements());
        meta.setPageNumber(categoryPage.getNumber() + 1);
        meta.setPageSize(categoryPage.getSize());

        return new GlobalResponseHandler().handleResponse("Categories retrieved successfully",
                categoryPage.getContent(), HttpStatus.OK, meta);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category category, HttpServletRequest request) {
        Optional<Category> foundProduct = categoryRepository.findById(id);
        if(foundProduct.isPresent()) {
            category.setId(id);
            categoryRepository.save(category);
            return new GlobalResponseHandler().handleResponse("Category updated successfully",
                category, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category id " + id + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public Category addCategory(@RequestBody Category category) {
        return  categoryRepository.save(category);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteCategory (@PathVariable Long id) {
        categoryRepository.deleteById(id);
    }
}
