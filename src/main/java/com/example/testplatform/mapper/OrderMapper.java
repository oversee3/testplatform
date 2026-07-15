package com.example.testplatform.mapper;

import com.example.testplatform.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO `order` (order_no, user_id, product_id, quantity, total_price, status) " +
            "VALUES (#{orderNo}, #{userId}, #{productId}, #{quantity}, #{totalPrice}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT * FROM `order` WHERE id = #{id} AND is_deleted = 0")
    @Results({
            @Result(property = "orderNo", column = "order_no"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "totalPrice", column = "total_price"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    Order findById(Long id);

    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo} AND is_deleted = 0")
    @Results({
            @Result(property = "orderNo", column = "order_no"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "totalPrice", column = "total_price"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    Order findByOrderNo(String orderNo);

    @Select("<script>" +
            "SELECT COUNT(*) FROM `order` WHERE is_deleted = 0 " +
            "<if test='userId != null'>AND user_id = #{userId} </if>" +
            "<if test='status != null'>AND status = #{status} </if>" +
            "</script>")
    long count(@Param("userId") Long userId,
               @Param("status") Integer status);

    @Select("<script>" +
            "SELECT * FROM `order` WHERE is_deleted = 0 " +
            "<if test='userId != null'>AND user_id = #{userId} </if>" +
            "<if test='status != null'>AND status = #{status} </if>" +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    @Results({
            @Result(property = "orderNo", column = "order_no"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "productId", column = "product_id"),
            @Result(property = "totalPrice", column = "total_price"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    List<Order> findPage(@Param("offset") int offset,
                         @Param("size") int size,
                         @Param("userId") Long userId,
                         @Param("status") Integer status);

    @Update("UPDATE `order` SET status = #{status} WHERE id = #{id} AND is_deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE `order` SET is_deleted = 1 WHERE id = #{id}")
    int deleteById(Long id);
}