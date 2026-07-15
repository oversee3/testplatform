package com.example.testplatform.service;

import com.example.testplatform.common.BusinessException;
import com.example.testplatform.common.ResultCode;
import com.example.testplatform.entity.Product;
import com.example.testplatform.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    //新增商品
    public Long addProduct(Product product) {
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        productMapper.insert(product);
        return product.getId();
    }

    // 查询商品详情
    public Product getProductById(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    // 分页查询商品列表
    public List<Product> getProductList(int page, int size, String keyword, Integer status) {
        int offset = (page - 1) * size;
        return productMapper.findPage(offset, size, keyword, status);
    }

    // 查询总记录数
    public long getProductCount(String keyword, Integer status) {
        return productMapper.count(keyword, status);
    }

    // 更新商品
    public void updateProduct(Product product) {
        // 先检查商品是否存在
        getProductById(product.getId());
        int rows = productMapper.update(product);
        if (rows == 0) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
    }

    // 删除商品（逻辑删除）
    public void deleteProduct(Long id) {
        getProductById(id); // 先检查是否存在
        productMapper.deleteById(id);
    }

    // 扣减库存（业务方法）
    public void deductStock(Long id, Integer quantity) {
        // 先检查商品是否存在
        Product product = productMapper.findById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        // 再扣减库存
        int rows = productMapper.deductStock(id, quantity);
        if (rows == 0) {
            throw new BusinessException(ResultCode.STOCK_INSUFFICIENT);
        }
    }
}