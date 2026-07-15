package com.example.testplatform.controller;

import com.example.testplatform.common.Result;
import com.example.testplatform.entity.Product;
import com.example.testplatform.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "商品管理", description = "商品的增删改查、分页搜索、库存管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "新增商品", description = "添加新商品，名称不能为空，价格和库存不能小于0")
    @PostMapping
    public Result<Map<String, Object>> addProduct(@Valid @RequestBody Product product) {
        Long newId = productService.addProduct(product);
        Map<String, Object> data = new HashMap<>();
        data.put("id", newId);
        return Result.success("商品添加成功", data);
    }

    @Operation(summary = "查询商品详情", description = "根据商品ID查询商品信息")
    @GetMapping("/{id}")
    public Result<Product> getProduct(@Parameter(description = "商品ID") @PathVariable Long id) {
        Product product = productService.getProductById(id);
        return Result.success(product);
    }

    @Operation(summary = "查询商品列表", description = "分页查询商品列表，支持关键字搜索和状态筛选")
    @GetMapping("/list")
    public Result<Map<String, Object>> getProductList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态：1上架 0下架") @RequestParam(required = false) Integer status) {

        List<Product> list = productService.getProductList(page, size, keyword, status);
        long total = productService.getProductCount(keyword, status);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);

        return Result.success(data);
    }

    @Operation(summary = "更新商品", description = "更新商品信息")
    @PutMapping
    public Result<String> updateProduct(@Valid @RequestBody Product product) {
        productService.updateProduct(product);
        return Result.success("商品更新成功", null);
    }

    @Operation(summary = "删除商品", description = "逻辑删除商品，数据不真正丢失")
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@Parameter(description = "商品ID") @PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("商品删除成功", null);
    }

    @Operation(summary = "扣减库存", description = "下单时扣减商品库存")
    @PostMapping("/deduct-stock")
    public Result<String> deductStock(@RequestBody Map<String, Object> params) {
        Long id = ((Number) params.get("id")).longValue();
        Integer quantity = (Integer) params.get("quantity");
        productService.deductStock(id, quantity);
        return Result.success("扣减库存成功", null);
    }
}