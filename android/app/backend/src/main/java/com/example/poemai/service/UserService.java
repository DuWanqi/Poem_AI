package com.example.poemai.service;

import com.example.poemai.model.User;
import com.example.poemai.repository.UserRepository;

import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository, 
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 注册方法
    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
         // ✅ 只在Service层进行一次加密
        String encodedPassword = passwordEncoder.encode(password);
    
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encodedPassword); // 存储单次加密密码
        return userRepository.save(user);
    }

    // ✅ 添加Controller需要的方法
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // // 认证方法
    // @Override
    // public UserDetails loadUserByUsername(String username) 
    //     throws UsernameNotFoundException {
        
    //     User user = userRepository.findByUsername(username)
    //         .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        
    //     return org.springframework.security.core.userdetails.User.builder()
    //         .username(user.getUsername())
    //         .password(user.getPasswordHash())
    //         .roles("USER")
    //         .build();
    // }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("正在加载用户: " + username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                System.out.println("用户不存在: " + username);
                return new UsernameNotFoundException("用户不存在: " + username);
            });

        System.out.println("找到用户: " + user.getUsername() + ", enabled: " + user.isEnabled() + ", locked: " + user.isAccountLocked());

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(username);
        builder.password(user.getPasswordHash());
        builder.disabled(!user.isEnabled());
        builder.accountLocked(user.isAccountLocked());
        builder.authorities("USER"); // 给用户分配基本权限

        return builder.build();
    }
}