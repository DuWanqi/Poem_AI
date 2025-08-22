package com.example.poemai.service;

import com.example.poemai.model.CiPai;
import com.example.poemai.repository.CiPaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CiPaiService {

    @Autowired
    private CiPaiRepository ciPaiRepository;
    // private final ObjectMapper objectMapper = new ObjectMapper();
    public List<CiPai> getAllCiPais() {
        return ciPaiRepository.findAll();
    }

    public CiPai getCiPaiById(Long id) {
        return ciPaiRepository.findById(id).orElseThrow(() -> new RuntimeException("词牌不存在"));
    }

    // public List<CiPai> matchCiPaiByLengths(List<Integer> lengths) {
    //     return ciPaiRepository.findBySentenceLengths(lengths);
    // }
    public List<CiPai> matchCiPaiByLengths(List<List<Integer>> lengths) {
        if (lengths.isEmpty()) return Collections.emptyList();
        
        System.out.println("请求的句式长度: " + lengths);
        
        // 获取所有词牌
        List<CiPai> allCiPais = ciPaiRepository.findAll();
        System.out.println("数据库中的词牌总数: " + allCiPais.size());
        
        // 过滤出匹配所有句式长度的词牌
        List<CiPai> matchedCiPais = allCiPais.stream()
            .filter(cipai -> {
                // 检查词牌的sentenceLengths是否包含所有请求的句式长度
                if (cipai.getSentenceLengths() == null) {
                    System.out.println("词牌 " + cipai.getName() + " 的sentenceLengths为null");
                    return false;
                }
                
                // System.out.println("检查词牌: " + cipai.getName() + ", 句式: " + java.util.Arrays.toString(cipai.getSentenceLengths()));
                
                for (List<Integer> requiredLength : lengths) {
                    boolean found = false;
                    for (String storedLength : cipai.getSentenceLengths()) {
                        // 将存储的字符串转换回List<Integer>格式进行比较
                        String requiredStr = requiredLength.toString().replace(" ", "");
                        if (storedLength.replace(" ", "").equals(requiredStr)) {
                            found = true;
                            // System.out.println("找到匹配的句式: " + requiredStr);
                            break;
                        }
                    }
                    // 如果任何一个所需的句式长度未找到，则排除该词牌
                    if (!found) {
                        // System.out.println("词牌 " + cipai.getName() + " 缺少句式: " + requiredLength);
                        return false;
                    }
                }
                System.out.println("词牌 " + cipai.getName() + " 匹配所有句式");
                return true;
            })
            .collect(Collectors.toList());
            
        System.out.println("匹配到的词牌数量: " + matchedCiPais.size());
        return matchedCiPais;
    }
}