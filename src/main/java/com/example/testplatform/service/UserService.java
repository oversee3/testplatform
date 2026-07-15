package com.example.testplatform.service;

import com.example.testplatform.common.BusinessException;
import com.example.testplatform.common.PasswordEncoder;
import com.example.testplatform.common.ResultCode;
import com.example.testplatform.entity.User;
import com.example.testplatform.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    public User getUserByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    public void register(User user) {
        // 删除手动校验 username/password 是否为空的代码
        // 因为 @Valid 已经帮我们校验了

        // 检查用户名是否已存在
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 密码加密（核心改动）
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userMapper.insert(user);
    }
    public void resetPassword(String username, String newPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        System.out.println("准备更新密码，username: " + username);
        System.out.println("新密文: " + encodedPassword);

        int rows = userMapper.updatePassword(user);
        System.out.println("更新影响行数: " + rows);   // 👈 关键：看这里是 0 还是 1
    }
}