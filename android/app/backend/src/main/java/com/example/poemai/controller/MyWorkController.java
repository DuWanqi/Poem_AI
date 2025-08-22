package com.example.poemai.controller;

import com.example.poemai.dto.SaveMyWorkRequest;
import com.example.poemai.dto.MyWorkResponse; // 关键修复1：导入MyWorkResponse类
import com.example.poemai.model.MyWork;
import com.example.poemai.repository.UserRepository;
import com.example.poemai.service.MyWorkService;
import com.example.poemai.utils.JwtUtils; // 关键修复2：导入JwtUtils类
import com.example.poemai.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work")
public class MyWorkController {

    @Autowired
    private MyWorkService myWorkService;
    
    @Autowired // 关键修复3：注入JwtUtils
    private JwtUtils jwtUtils;

    @Autowired // ✅ 新增
    private UserRepository userRepository;
    /**
     * 保存作品
     */
    @PostMapping("/save")
    public Result<Map<String, Object>> saveWork(@RequestBody SaveMyWorkRequest request, @RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromToken(token);
        Map<String, Object> response = myWorkService.saveWork(request, userId);
        return Result.success(response);
    }

    /**
     * 获取用户所有作品
     */
    @GetMapping("/")
    public Result<List<MyWork>> getAllWorks(@RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromToken(token);
        List<MyWork> works = myWorkService.getAllWorksByUserId(userId);
        return Result.success(works);
    }

    /**
     * 获取单个作品详情
     */
    @GetMapping("/{id}")
    public Result<MyWorkResponse> getWorkById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromToken(token);
        
        // 通过service获取作品，避免在Controller中直接访问Repository
        MyWorkResponse response = myWorkService.getWorkById(id, userId);
        return Result.success(response);
    }

    /**
     * 删除作品
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteWork(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        Long userId = extractUserIdFromToken(token);
        myWorkService.deleteWorkByIdAndUserId(id, userId);
        return Result.success("删除成功");
    }
    // 就是这个地方 
    // private Long extractUserIdFromToken(String token) {
    //     // 关键修复4：实现JWT解析逻辑
    //     String jwtToken = token.replace("Bearer ", "");
    //     try {
    //         String username = jwtUtils.extractUsername(jwtToken);
    //         // 在实际应用中，这里应该根据用户名查询用户ID
    //         // 简化处理：假设用户名就是用户ID的数字形式
    //         return Long.parseLong(username);
    //     } catch (Exception e) {
    //         throw new RuntimeException("无效的Token");
    //     }
    // }
    private Long extractUserIdFromToken(String token) {
    String jwtToken = token.replace("Bearer ", "");
    
    try {
        String username = jwtUtils.extractUsername(jwtToken);
        return userRepository.findIdByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    } catch (Exception e) {
        throw new AuthenticationCredentialsNotFoundException("Token验证失败: " + e.getMessage());
    }
}
}