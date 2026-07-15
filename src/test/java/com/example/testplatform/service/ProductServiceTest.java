package com.example.testplatform.service;

import com.example.testplatform.common.BusinessException;
import com.example.testplatform.common.ResultCode;
import com.example.testplatform.entity.Product;
import com.example.testplatform.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试商品");
        testProduct.setDescription("这是一个测试商品");
        testProduct.setPrice(new BigDecimal("99.00"));
        testProduct.setStock(100);
        testProduct.setStatus(1);
        testProduct.setIsDeleted(0);
    }

    // ===== 测试：新增商品成功 =====
    @Test
    void addProduct_Success() {
        when(productMapper.insert(any(Product.class))).thenReturn(1);

        assertDoesNotThrow(() -> productService.addProduct(testProduct));

        verify(productMapper, times(1)).insert(any(Product.class));
    }

    // ===== 测试：新增商品时默认状态为上架 =====
    @Test
    void addProduct_DefaultStatus() {
        Product newProduct = new Product();
        newProduct.setName("新商品");
        newProduct.setPrice(new BigDecimal("50.00"));
        newProduct.setStock(10);
        newProduct.setStatus(null);  // 不设置状态

        when(productMapper.insert(any(Product.class))).thenReturn(1);

        productService.addProduct(newProduct);

        // 验证：状态被设置为 1（上架）
        assertEquals(1, newProduct.getStatus());
    }

    // ===== 测试：查询商品 - 成功 =====
    @Test
    void getProductById_Success() {
        when(productMapper.findById(1L)).thenReturn(testProduct);

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("测试商品", result.getName());
        assertEquals(new BigDecimal("99.00"), result.getPrice());
    }

    // ===== 测试：查询商品 - 不存在 =====
    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productMapper.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.getProductById(999L));

        assertEquals(ResultCode.PRODUCT_NOT_FOUND, exception.getResultCode());
    }

    // ===== 测试：扣减库存 - 成功 =====
    @Test
    void deductStock_Success() {
        when(productMapper.findById(1L)).thenReturn(testProduct);
        when(productMapper.deductStock(1L, 5)).thenReturn(1);

        assertDoesNotThrow(() -> productService.deductStock(1L, 5));

        verify(productMapper, times(1)).deductStock(1L, 5);
    }

    // ===== 测试：扣减库存 - 商品不存在 =====
    @Test
    void deductStock_ProductNotFound_ThrowsException() {
        when(productMapper.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.deductStock(999L, 5));

        assertEquals(ResultCode.PRODUCT_NOT_FOUND, exception.getResultCode());
        verify(productMapper, never()).deductStock(anyLong(), anyInt());
    }

    // ===== 测试：扣减库存 - 库存不足 =====
    @Test
    void deductStock_InsufficientStock_ThrowsException() {
        when(productMapper.findById(1L)).thenReturn(testProduct);
        when(productMapper.deductStock(1L, 999)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.deductStock(1L, 999));

        assertEquals(ResultCode.STOCK_INSUFFICIENT, exception.getResultCode());
    }

    // ===== 测试：查询商品列表 =====
    @Test
    void getProductList_Success() {
        List<Product> mockList = Arrays.asList(testProduct);
        when(productMapper.findPage(0, 10, null, null)).thenReturn(mockList);
        when(productMapper.count(null, null)).thenReturn(1L);

        List<Product> result = productService.getProductList(1, 10, null, null);
        long total = productService.getProductCount(null, null);

        assertEquals(1, result.size());
        assertEquals(1L, total);
        assertEquals("测试商品", result.get(0).getName());
    }

    // ===== 测试：删除商品（逻辑删除） =====
    @Test
    void deleteProduct_Success() {
        when(productMapper.findById(1L)).thenReturn(testProduct);
        when(productMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productMapper, times(1)).deleteById(1L);
    }

    // ===== 测试：删除商品 - 不存在 =====
    @Test
    void deleteProduct_NotFound_ThrowsException() {
        when(productMapper.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.deleteProduct(999L));

        assertEquals(ResultCode.PRODUCT_NOT_FOUND, exception.getResultCode());
        verify(productMapper, never()).deleteById(anyLong());
    }
}