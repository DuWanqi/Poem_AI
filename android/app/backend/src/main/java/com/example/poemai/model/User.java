package com.example.poemai.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    // @Column(nullable = false)
    // private String createdAt = LocalDateTime.now().toString();

    // @Column(nullable = false)
    // private String updatedAt = LocalDateTime.now().toString();
    // 将String类型改为LocalDateTime
    @Column(name = "created_at")
    private LocalDateTime createdAt;  // 匹配数据库TIMESTAMP类型

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  //  同上
    
    // 添加enabled字段
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true; // 默认值

    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(); // 每次更新时刷新
    }
}