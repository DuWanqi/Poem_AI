package com.example.poemai.controller;

import com.example.poemai.dto.CiPaiMatchRequest;
// import com.example.poemai.dto.SentenceLengthRequest; // 导入新DTO
import com.example.poemai.model.CiPai;
import com.example.poemai.service.CiPaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cipai")
public class CiPaiController {

    @Autowired
    private CiPaiService ciPaiService;

    @GetMapping("/{id}")
    public ResponseEntity<CiPai> getCiPai(@PathVariable Long id) {
        return ResponseEntity.ok(ciPaiService.getCiPaiById(id));
    }

    @GetMapping("/")
    public ResponseEntity<List<CiPai>> getAllCiPais() {
        return ResponseEntity.ok(ciPaiService.getAllCiPais());
    }

    // @PostMapping("/match")
    // public ResponseEntity<List<CiPai>> matchCiPai(@RequestBody SentenceLengthRequest request) {
    //     // ✅ 添加请求日志
    //     System.out.println("接收到词牌匹配请求，参数: " + request.getLengths());
    //     return ResponseEntity.ok(ciPaiService.matchCiPaiByLengths(request.getLengths()));
    // }
    @PostMapping("/match")
    public ResponseEntity<?> matchCiPai(
        @RequestBody CiPaiMatchRequest request // 使用DTO
    ) {
        
        System.out.println("收到请求: " + request.getLengths());
        System.out.println("转换参数: " + request.getLengths().get(0).toString());
        return ResponseEntity.ok(ciPaiService.matchCiPaiByLengths(request.getLengths()));
    }

}