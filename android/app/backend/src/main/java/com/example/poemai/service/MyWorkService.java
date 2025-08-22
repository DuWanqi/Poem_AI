package com.example.poemai.service;

import com.example.poemai.model.MyWork;
import com.example.poemai.repository.MyWorkRepository;
import com.example.poemai.dto.MyWorkResponse;
import com.example.poemai.dto.SaveMyWorkRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MyWorkService {

    @Autowired
    private MyWorkRepository myWorkRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 保存用户的作品
     */
    public Map<String, Object> saveWork(SaveMyWorkRequest request, Long userId) {
        MyWork work = new MyWork();
        work.setUserId(userId);
        work.setTitle(request.getTitle());
        work.setContent(request.getContent());
        work.setWorkType(request.getWorkType());
        work.setFontSetting(mapToJsonNode(request.getFontSetting()));
        work.setBackgroundInfo(mapToJsonNode(request.getBackgroundInfo()));
        work.setAssociatedCipaiId(request.getAssociatedCipaiId());
        work.setTemplateHighlightIndex(mapToJsonNode(request.getTemplateHighlightIndex()));

        MyWork savedWork = myWorkRepository.save(work);

        Map<String, Object> result = new HashMap<>();
        result.put("id", savedWork.getId());
        result.put("title", savedWork.getTitle());
        result.put("content", savedWork.getContent());
        result.put("workType", savedWork.getWorkType());
        result.put("updatedAt", savedWork.getUpdatedAt());

        return result;
    }

    /**
     * 获取用户的全部作品
     */
    public List<MyWork> getAllWorksByUserId(Long userId) {
        return myWorkRepository.findByUserId(userId);
    }

    /**
     * 将 Map 转换为 JsonNode
     */
    private JsonNode mapToJsonNode(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        return objectMapper.valueToTree(map);
    }
    
    public void deleteWorkByIdAndUserId(Long workId, Long userId) {
        MyWork work = myWorkRepository.findById(workId)
            .orElseThrow(() -> new RuntimeException("作品不存在"));

        if (!work.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人作品");
        }

        myWorkRepository.delete(work);
    }
    
    public MyWorkResponse getWorkById(Long id, Long userId) {
        MyWork work = myWorkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("作品不存在"));
        
        if (!work.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问他人作品");
        }
        
        return new MyWorkResponse(work);
    }
}