package com.example.testplatform.service;

import com.example.testplatform.common.BusinessException;
import com.example.testplatform.common.ResultCode;
import com.example.testplatform.entity.Order;
import com.example.testplatform.entity.Product;
import com.example.testplatform.mapper.OrderMapper;
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
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private OrderService orderService;

    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试商品");
        testProduct.setPrice(new BigDecimal("99.00"));
        testProduct.setStock(100);
        testProduct.setStatus(1);
        testProduct.setIsDeleted(0);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD20260713152349TEST");
        testOrder.setUserId(1L);
        testOrder.setProductId(1L);
        testOrder.setQuantity(2);
        testOrder.setTotalPrice(new BigDecimal("198.00"));
        testOrder.setStatus(1); // 待支付
        testOrder.setIsDeleted(0);
    }

    // ===== 测试：创建订单成功 =====
    @Test
    void createOrder_Success() {
        when(productMapper.findById(1L)).thenReturn(testProduct);
        when(productMapper.deductStock(1L, 2)).thenReturn(1);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);

        Order result = orderService.createOrder(1L, 1L, 2);

        assertNotNull(result);
        assertNotNull(result.getOrderNo());
        assertEquals(1L, result.getUserId());
        assertEquals(1L, result.getProductId());
        assertEquals(2, result.getQuantity());
        assertEquals(new BigDecimal("198.00"), result.getTotalPrice());
        assertEquals(1, result.getStatus()); // 待支付

        verify(productMapper, times(1)).deductStock(1L, 2);
        verify(orderMapper, times(1)).insert(any(Order.class));
    }

    // ===== 测试：创建订单 - 商品不存在 =====
    @Test
    void createOrder_ProductNotFound_ThrowsException() {
        when(productMapper.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, 999L, 2));

        assertEquals(ResultCode.PRODUCT_NOT_FOUND, exception.getResultCode());
        verify(productMapper, never()).deductStock(anyLong(), anyInt());
        verify(orderMapper, never()).insert(any(Order.class));
    }

    // ===== 测试：创建订单 - 库存不足 =====
    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        when(productMapper.findById(1L)).thenReturn(testProduct);
        when(productMapper.deductStock(1L, 999)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, 1L, 999));

        assertEquals(ResultCode.STOCK_INSUFFICIENT, exception.getResultCode());
        verify(orderMapper, never()).insert(any(Order.class));
    }

    // ===== 测试：查询订单 - 成功 =====
    @Test
    void getOrderById_Success() {
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals("ORD20260713152349TEST", result.getOrderNo());
        assertEquals(1L, result.getUserId());
    }

    // ===== 测试：查询订单 - 不存在 =====
    @Test
    void getOrderById_NotFound_ThrowsException() {
        when(orderMapper.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.getOrderById(999L));

        assertEquals(ResultCode.ORDER_NOT_FOUND, exception.getResultCode());
    }

    // ===== 测试：取消订单 - 待支付成功 =====
    @Test
    void cancelOrder_Pending_Success() {
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(productMapper.updateStockAdd(1L, 2)).thenReturn(1);
        when(orderMapper.updateStatus(1L, 3)).thenReturn(1);

        assertDoesNotThrow(() -> orderService.cancelOrder(1L));

        verify(productMapper, times(1)).updateStockAdd(1L, 2);
        verify(orderMapper, times(1)).updateStatus(1L, 3);
    }

    // ===== 测试：取消订单 - 已支付成功（退款） =====
    @Test
    void cancelOrder_Paid_Success() {
        testOrder.setStatus(2); // 已支付
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(productMapper.updateStockAdd(1L, 2)).thenReturn(1);
        when(orderMapper.updateStatus(1L, 3)).thenReturn(1);

        assertDoesNotThrow(() -> orderService.cancelOrder(1L));

        verify(productMapper, times(1)).updateStockAdd(1L, 2);
        verify(orderMapper, times(1)).updateStatus(1L, 3);
    }

    // ===== 测试：取消订单 - 已发货不可取消 =====
    @Test
    void cancelOrder_Shipped_ThrowsException() {
        testOrder.setStatus(3); // 已发货
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(1L));

        assertEquals(ResultCode.ORDER_CANNOT_CANCEL, exception.getResultCode());
        verify(productMapper, never()).updateStockAdd(anyLong(), anyInt());
        verify(orderMapper, never()).updateStatus(anyLong(), anyInt());
    }

    // ===== 测试：取消订单 - 已完成不可取消 =====
    @Test
    void cancelOrder_Completed_ThrowsException() {
        testOrder.setStatus(4); // 已完成
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(1L));

        assertEquals(ResultCode.ORDER_CANNOT_CANCEL, exception.getResultCode());
        verify(productMapper, never()).updateStockAdd(anyLong(), anyInt());
        verify(orderMapper, never()).updateStatus(anyLong(), anyInt());
    }

    // ===== 测试：支付订单成功 =====
    @Test
    void payOrder_Success() {
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.updateStatus(1L, 2)).thenReturn(1);

        assertDoesNotThrow(() -> orderService.payOrder(1L));

        verify(orderMapper, times(1)).updateStatus(1L, 2);
    }

    // ===== 测试：支付订单 - 订单不存在 =====
    @Test
    void payOrder_NotFound_ThrowsException() {
        when(orderMapper.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.payOrder(999L));

        assertEquals(ResultCode.ORDER_NOT_FOUND, exception.getResultCode());
        verify(orderMapper, never()).updateStatus(anyLong(), anyInt());
    }

    // ===== 测试：支付订单 - 状态异常（已支付不能再次支付） =====
    @Test
    void payOrder_AlreadyPaid_ThrowsException() {
        testOrder.setStatus(2); // 已支付
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.payOrder(1L));

        assertEquals(ResultCode.ORDER_STATUS_ERROR, exception.getResultCode());
        verify(orderMapper, never()).updateStatus(anyLong(), anyInt());
    }

    // ===== 测试：订单列表查询 =====
    @Test
    void getOrderList_Success() {
        List<Order> mockList = Arrays.asList(testOrder);
        when(orderMapper.findPage(0, 10, 1L, 1)).thenReturn(mockList);
        when(orderMapper.count(1L, 1)).thenReturn(1L);

        List<Order> result = orderService.getOrderList(1, 10, 1L, 1);
        long total = orderService.getOrderCount(1L, 1);

        assertEquals(1, result.size());
        assertEquals(1L, total);
        assertEquals("ORD20260713152349TEST", result.get(0).getOrderNo());
    }
}