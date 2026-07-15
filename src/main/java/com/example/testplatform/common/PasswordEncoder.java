package com.example.testplatform.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 加密：把明文密码加密成密文
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    // 验证：比对明文密码和密文是否匹配
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}