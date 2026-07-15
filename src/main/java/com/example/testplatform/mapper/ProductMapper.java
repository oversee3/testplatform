package com.example.testplatform.mapper;

import com.example.testplatform.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {

    // 新增商品
    @Insert("INSERT INTO product (name, description, price, stock, status) " +
            "VALUES (#{name}, #{description}, #{price}, #{stock}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    // 根据ID查询（含所有字段）
    @Select("SELECT id, name, description, price, stock, status, is_deleted, created_at, updated_at " +
            "FROM product WHERE id = #{id} AND is_deleted = 0")
    @Results({
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    Product findById(Long id);

    // 分页查询商品列表
    @Select("<script>" +
            "SELECT id, name, description, price, stock, status, is_deleted, created_at, updated_at " +
            "FROM product WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY id DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    @Results({
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    List<Product> findPage(@Param("offset") int offset,
                           @Param("size") int size,
                           @Param("keyword") String keyword,
                           @Param("status") Integer status);

    // 查询总记录数
    @Select("<script>" +
            "SELECT COUNT(*) FROM product WHERE is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "</script>")
    long count(@Param("keyword") String keyword,
               @Param("status") Integer status);

    // 更新商品
    @Update("UPDATE product SET name = #{name}, description = #{description}, " +
            "price = #{price}, stock = #{stock}, status = #{status} " +
            "WHERE id = #{id} AND is_deleted = 0")
    int update(Product product);

    // 逻辑删除
    @Update("UPDATE product SET is_deleted = 1 WHERE id = #{id}")
    int deleteById(Long id);

    // 扣减库存
    @Update("UPDATE product SET stock = stock - #{quantity} " +
            "WHERE id = #{id} AND stock >= #{quantity} AND is_deleted = 0")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    // 恢复库存（取消订单时使用）
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id} AND is_deleted = 0")
    int updateStockAdd(@Param("id") Long id, @Param("quantity") Integer quantity);

}