package com.example.testplatform.controller;

import com.example.testplatform.common.Result;
import com.example.testplatform.entity.Order;
import com.example.testplatform.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "订单管理", description = "订单创建、支付、取消、查询")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "创建订单", description = "用户下单，自动扣减库存，生成唯一订单号")
    @PostMapping
    public Result<Order> createOrder(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("userId")).longValue();
        Long productId = ((Number) params.get("productId")).longValue();
        Integer quantity = (Integer) params.get("quantity");

        Order order = orderService.createOrder(userId, productId, quantity);
        return Result.success("下单成功", order);
    }

    @Operation(summary = "查询订单详情", description = "根据订单ID查询订单信息")
    @GetMapping("/{id}")
    public Result<Order> getOrder(@Parameter(description = "订单ID") @PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return Result.success(order);
    }

    @Operation(summary = "查询订单列表", description = "分页查询订单列表，支持按用户和状态筛选")
    @GetMapping("/list")
    public Result<Map<String, Object>> getOrderList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "状态：1待支付 2已支付 3已取消 4已完成") @RequestParam(required = false) Integer status) {

        List<Order> list = orderService.getOrderList(page, size, userId, status);
        long total = orderService.getOrderCount(userId, status);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);

        return Result.success(data);
    }

    @Operation(summary = "取消订单", description = "待支付订单释放库存，已支付订单退款+释放库存，已发货/已完成不可取消")
    @PutMapping("/cancel/{id}")
    public Result<String> cancelOrder(@Parameter(description = "订单ID") @PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success("订单已取消", null);
    }

    @Operation(summary = "支付订单", description = "模拟支付成功，更新订单状态为已支付")
    @PutMapping("/pay/{id}")
    public Result<String> payOrder(@Parameter(description = "订单ID") @PathVariable Long id) {
        orderService.payOrder(id);
        return Result.success("支付成功", null);
    }
}