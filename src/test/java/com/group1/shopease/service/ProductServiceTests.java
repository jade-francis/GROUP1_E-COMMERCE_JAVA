package com.group1.shopease.service;

import com.group1.shopease.exception.ProductNotFoundException;
import com.group1.shopease.model.Product;
import com.group1.shopease.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTests {

    private final ProductRepository repository = mock(ProductRepository.class);
    private final ProductService service = new ProductService(repository);

    @Test
    void findByIdReturnsExistingProduct() {
        Product product = product(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        assertEquals(product, service.findById(1L));
    }

    @Test
    void findByIdThrowsWhenProductDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void createIgnoresAnIdSuppliedByTheClient() {
        Product product = product(50L);
        when(repository.save(product)).thenReturn(product);

        service.create(product);

        assertEquals(null, product.getId());
        verify(repository).save(product);
    }

    @Test
    void deleteThrowsWhenNoRowWasDeleted() {
        when(repository.deleteById(99L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> service.delete(99L));
    }

    private Product product(Long id) {
        return new Product(
                id,
                "Wireless Mouse",
                "Ergonomic mouse",
                new BigDecimal("12000.00"),
                25,
                1L,
                "/images/mouse.jpg"
        );
    }
}
