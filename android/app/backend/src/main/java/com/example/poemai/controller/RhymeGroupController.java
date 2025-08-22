package com.example.poemai.controller;

import com.example.poemai.service.RhymeGroupService;
import com.example.poemai.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rhyme")
public class RhymeGroupController {

    @Autowired
    private RhymeGroupService rhymeGroupService;

    @GetMapping("/words")
    public Result<?> getRhymeWords(@RequestParam String query) {
        System.out.println("接收到查询请求: " + query);
        
        if (query == null || query.trim().isEmpty()) {
            return Result.error("请输入查询字");
        }

        try {
            Map<String, Object> result = rhymeGroupService.getRhymeInfoByChar(query.trim());
            
            if (result.isEmpty()) {
                return Result.success(Map.of(
                    "rhymeGroup", "未找到", 
                    "words", List.of(),
                    "message", "未找到字符 '" + query + "' 对应的押韵组"
                ));
            }
            
            return Result.success(result);
            
        } catch (Exception e) {
            System.err.println("查询押韵字时出错: " + e.getMessage());
            e.printStackTrace();
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}