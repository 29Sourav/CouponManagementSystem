package com.example.CMS.service;

import com.example.CMS.entity.Product;

import java.util.List;

public interface ProductService {

    Product create(Product product);

    List<Product> getAll();

    Product getById(Long id);
}
