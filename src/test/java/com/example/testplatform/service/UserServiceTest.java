package com.example.testplatform.service;

import com.example.testplatform.common.BusinessException;
import com.example.testplatform.common.PasswordEncoder;
import com.example.testplatform.common.ResultCode;
import com.example.testplatform.entity.User;
import com.example.testplatform.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;  // 模拟 Mapper

    @Mock
    private PasswordEncoder passwordEncoder;  // 模拟密码加密

    @InjectMocks
    private UserService userService;  // 注入模拟对象

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("123456");
        testUser.setEmail("test@test.com");
    }

    // ===== 测试：注册成功 =====
    @Test
    void register_Success() {
        // 1. 模拟：用户名不存在
        when(userMapper.findByUsername("testuser")).thenReturn(null);
        // 2. 模拟：密码加密
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        // 3. 模拟：插入成功
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // 执行
        assertDoesNotThrow(() -> userService.register(testUser));

        // 验证：密码被加密
        assertEquals("encodedPassword", testUser.getPassword());
        // 验证：insert 被调用了一次
        verify(userMapper, times(1)).insert(any(User.class));
    }

    // ===== 测试：注册失败 - 用户名已存在 =====
    @Test
    void register_UsernameExists_ThrowsException() {
        // 模拟：用户名已存在
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        // 执行 & 验证：应该抛出 BusinessException
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(testUser));

        assertEquals(ResultCode.USERNAME_EXISTS, exception.getResultCode());
        // 验证：insert 没有被调用
        verify(userMapper, never()).insert(any(User.class));
    }

    // ===== 测试：查询用户 - 成功 =====
    @Test
    void getUserById_Success() {
        when(userMapper.findById(1L)).thenReturn(testUser);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    // ===== 测试：查询用户 - 不存在 =====
    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userMapper.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.getUserById(999L));

        assertEquals(ResultCode.USER_NOT_FOUND, exception.getResultCode());
    }
}