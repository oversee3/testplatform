package com.example.testplatform.service;

import com.example.testplatform.common.BusinessException;
import com.example.testplatform.common.ResultCode;
import com.example.testplatform.entity.Order;
import com.example.testplatform.entity.Product;
import com.example.testplatform.mapper.OrderMapper;
import com.example.testplatform.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    // 生成订单编号
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD" + timestamp + uuid;
    }

    /**
     * 创建订单（核心业务 - 需要事务）
     * 1. 检查商品是否存在
     * 2. 扣减库存
     * 3. 生成订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long productId, Integer quantity) {
        // 1. 查询商品
        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 扣减库存（扣库存失败会抛出异常）
        int rows = productMapper.deductStock(productId, quantity);
        if (rows == 0) {
            throw new BusinessException(ResultCode.STOCK_INSUFFICIENT);
        }

        // 3. 生成订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalPrice(product.getPrice().multiply(new java.math.BigDecimal(quantity)));
        order.setStatus(1); // 待支付

        orderMapper.insert(order);
        return order;
    }

    // 查询订单详情
    public Order getOrderById(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    // 查询订单列表
    public List<Order> getOrderList(int page, int size, Long userId, Integer status) {
        int offset = (page - 1) * size;
        return orderMapper.findPage(offset, size, userId, status);
    }

    // 查询订单总数
    public long getOrderCount(Long userId, Integer status) {
        return orderMapper.count(userId, status);
    }

    /**
     * 取消订单（支持待支付取消和已支付退款）
     * 待支付(1) → 释放库存
     * 已支付(2) → 释放库存 + 模拟退款
     * 已发货(3)、已完成(4) → 不可取消
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        // 直接查数据库，不用调用本类方法
        Order order = orderMapper.findById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        // 已发货、已完成不能取消
        if (order.getStatus() == 3 || order.getStatus() == 4) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL);
        }

        // ===== 1. 处理库存和退款 =====
        if (order.getStatus() == 1) {
            // 待支付：释放库存
            productMapper.updateStockAdd(order.getProductId(), order.getQuantity());
            System.out.println("📦 库存恢复：商品ID=" + order.getProductId() +
                    "，数量=" + order.getQuantity());
        }

        if (order.getStatus() == 2) {
            // 已支付：释放库存 + 模拟退款
            productMapper.updateStockAdd(order.getProductId(), order.getQuantity());
            System.out.println("📦 库存恢复：商品ID=" + order.getProductId() +
                    "，数量=" + order.getQuantity());

            // 模拟退款（实际项目要调用支付网关接口）
            System.out.println("💳 退款中：订单号=" + order.getOrderNo() +
                    "，退款金额=" + order.getTotalPrice());
            // 假设退款成功
            System.out.println("✅ 退款成功！");
        }

        // ===== 2. 更新订单状态为已取消 =====
        orderMapper.updateStatus(id, 3);
    }

    /**
     * 支付成功回调
     * 更新订单状态为已支付
     */
    public void payOrder(Long id) {
        Order order = getOrderById(id);

        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "当前订单不可支付");
        }

        orderMapper.updateStatus(id, 2);
    }
}