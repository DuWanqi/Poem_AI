package com.example.poemai.service;

// import com.example.poemai.model.RhymeGroup;
import com.example.poemai.repository.RhymeGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

@Service
public class RhymeGroupService {

    @Autowired
    private RhymeGroupRepository rhymeGroupRepository;

    public List<String> getRhymeWordsByChar(String ch) {
        System.out.println("查询字符: " + ch);
        
        return rhymeGroupRepository.findByCharacter(ch)
                .map(group -> {
                    System.out.println("找到押韵组: " + group.getGroupName());
                    List<String> words = group.getCharacterListAsStringList();
                    System.out.println("押韵字列表: " + words);
                    return words;
                })
                .orElseGet(() -> {
                    System.out.println("未找到字符 '" + ch + "' 对应的押韵组");
                    return Collections.emptyList(); // 返回空列表而不是抛异常
                });
    }
    
    // 新增方法：返回完整的押韵组信息
    public Map<String, Object> getRhymeInfoByChar(String ch) {
        System.out.println("查询字符完整信息: " + ch);
        
        return rhymeGroupRepository.findByCharacter(ch)
                .map(group -> {
                    System.out.println("找到押韵组: " + group.getGroupName());
                    List<String> words = group.getCharacterListAsStringList();
                    System.out.println("押韵字列表: " + words);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("rhymeGroup", group.getGroupName()); // 动态获取组名
                    result.put("words", words);
                    return result;
                })
                .orElseGet(() -> {
                    System.out.println("未找到字符 '" + ch + "' 对应的押韵组");
                    return Collections.emptyMap(); // 返回空Map
                });
    }
}