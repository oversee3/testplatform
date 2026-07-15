package com.example.testplatform.controller;

import com.example.testplatform.common.JwtUtil;
import com.example.testplatform.common.PasswordEncoder;
import com.example.testplatform.common.Result;
import com.example.testplatform.common.ResultCode;
import com.example.testplatform.entity.User;
import com.example.testplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户管理", description = "用户注册、登录、查询、密码重置")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "查询用户", description = "根据用户ID查询用户信息")
    @GetMapping("/{id}")
    public Result<User> getUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    @Operation(summary = "用户注册", description = "注册新用户，用户名不能重复")
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody User user) {
        userService.register(user);
        return Result.success("注册成功", null);
    }

    @Operation(summary = "用户登录", description = "登录成功后返回 JWT Token")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody User user) {
        User existing = userService.getUserByUsername(user.getUsername());
        if (existing == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }

        boolean matched = passwordEncoder.matches(user.getPassword(), existing.getPassword());
        if (!matched) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        String token = jwtUtil.generateToken(existing.getUsername());

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("username", existing.getUsername());

        return Result.success("登录成功", data);
    }

    @Operation(summary = "重置密码", description = "重置用户密码（直接更新为新密码）")
    @PutMapping("/reset-password")
    public Result<String> resetPassword(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        userService.resetPassword(username, newPassword);
        return Result.success("密码重置成功", null);
    }
}