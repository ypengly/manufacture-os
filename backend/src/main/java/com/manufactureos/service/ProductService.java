package com.manufactureos.service;

import com.manufactureos.entity.Product;
import com.manufactureos.dto.product.ProductRequest;
import com.manufactureos.dto.product.ProductResponse;
import com.manufactureos.mapper.ProductMapper;
import com.manufactureos.repository.ProductRepository;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final AuditService auditService;

    public ProductService(ProductRepository repository, ProductMapper mapper, AuditService auditService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(String q) {
        List<Product> all = repository.findAll();
        if (q != null && !q.isBlank()) {
            String needle = q.toLowerCase();
            all = all.stream().filter(item -> {
                String q2 = needle;
                return (item.getCode() != null && item.getCode().toLowerCase().contains(q2)) || (item.getSku() != null && item.getSku().toLowerCase().contains(q2)) || (item.getName() != null && item.getName().toLowerCase().contains(q2));
            }).toList();
        }
        return all.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest req) {
        Product e = new Product();
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("PRODUCT_CREATED", "Product", String.valueOf(e.getId()), "Product created");
        return mapper.toResponse(e);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Product e = find(id);
        mapper.applyToEntity(req, e);
        e = repository.save(e);
        auditService.log("PRODUCT_UPDATED", "Product", String.valueOf(e.getId()), "Product updated");
        return mapper.toResponse(e);
    }

    private Product find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }
}
