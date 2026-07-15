package com.example.testplatform.mapper;

import com.example.testplatform.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password, email, created_at AS createdAt FROM `user` WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT id, username, password, email, created_at AS createdAt FROM `user` WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO `user` (username, password, email, created_at) VALUES (#{username}, #{password}, #{email}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE `user` SET password = #{password} WHERE username = #{username}")
    int updatePassword(User user);   // 把 void 改成 int

    @Update("UPDATE product SET stock = stock - #{quantity} " +
            "WHERE id = #{id} AND stock >= #{quantity} AND is_deleted = 0")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}