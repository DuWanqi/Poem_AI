package com.example.poemai.controller;

import com.example.poemai.model.User;
import com.example.poemai.service.UserService;
import com.example.poemai.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // 必须导入这个包
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    // 添加PasswordEncoder自动注入
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // 使用passwordEncoder加密密码
        // String encodedPassword = passwordEncoder.encode(request.getPassword());
        // User user = userService.register(request.getUsername(), encodedPassword);
        // ❌ 移除Controller层的加密
        // ✅ 直接传递明文密码到Service
        User user = userService.register(request.getUsername(), request.getPassword());
        String token = jwtUtils.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // ✅ 添加调试日志（检查实际收到的密码）
        System.out.println("Login password received: " + request.getPassword());

        User user = userService.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 使用passwordEncoder验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtils.generateToken(user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getId()));
    }
    
    static class RegisterRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class AuthResponse {
        private String token;
        private Long userId;

        public AuthResponse(String token, Long userId) {
            this.token = token;
            this.userId = userId;
        }

        public String getToken() { return token; }
        public Long getUserId() { return userId; }
    }
}