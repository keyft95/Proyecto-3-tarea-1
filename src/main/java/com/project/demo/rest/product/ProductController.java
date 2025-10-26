package com.project.demo.rest.product;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalElements(productPage.getTotalElements());
        meta.setPageNumber(productPage.getNumber() + 1);
        meta.setPageSize(productPage.getSize());

        return new GlobalResponseHandler().handleResponse("Product retrieved succesfully",
                productPage.getContent(), HttpStatus.OK, meta);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN_ROLE')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product, HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(id);
        if (foundProduct.isPresent()) {
            product.setId(foundProduct.get().getId());
            product.setCategory(foundProduct.get().getCategory());
            productRepository.save(product);

            return new GlobalResponseHandler().handleResponse("Product updated succesfully",
                    product,
                    HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product id" + id + "not found",
                    HttpStatus.NOT_FOUND, request);

        }
    }

    @PostMapping("/{idCategory}")
    public ResponseEntity<?> addProductToCategory(@PathVariable Long idCategory, @RequestBody Product product, HttpServletRequest request) {
        Optional<Category> foundCategory = categoryRepository.findById(idCategory);
        if (foundCategory.isPresent()) {
            product.setCategory(foundCategory.get());
            Product saveProduct = productRepository.save(product);

            return new GlobalResponseHandler().handleResponse("Product created succesfully",
                    saveProduct, HttpStatus.CREATED, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category id " + idCategory + "not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN_ROLE')")
    @DeleteMapping("/{id}")
    public void deleteProduct (@PathVariable Long id) {
        productRepository.deleteById(id);
    }

}
