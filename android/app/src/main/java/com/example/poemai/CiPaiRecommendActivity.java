package com.example.poemai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poemai.model.CiPai;
import com.example.poemai.service.BackendService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CiPaiRecommendActivity extends AppCompatActivity {
    private TextView tvContent;
    private RecyclerView rvCiPaiList;
    private Button btnBack;
    private CiPaiAdapter ciPaiAdapter;
    private List<CiPai> ciPaiList;
    private String cardContent;
    private BackendService backendService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cipai_recommend);

        backendService = BackendService.getInstance(this);

        initViews();
        setupRecyclerView();
        loadContent();
        loadRecommendedCiPais();
    }

    private void initViews() {
        tvContent = findViewById(R.id.tvContent);
        rvCiPaiList = findViewById(R.id.rvCiPaiList);
        btnBack = findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        ciPaiList = new ArrayList<>();
        ciPaiAdapter = new CiPaiAdapter(ciPaiList, ciPai -> {
            // 点击词牌后进入推荐诗词展示界面
            Intent intent = new Intent(CiPaiRecommendActivity.this, PoemDisplayActivity.class);
            intent.putExtra("selected_cipai", ciPai);
            intent.putExtra("card_content", cardContent);
            startActivity(intent);
        });
        
        rvCiPaiList.setLayoutManager(new LinearLayoutManager(this));
        rvCiPaiList.setAdapter(ciPaiAdapter);
    }

    private void loadContent() {
        cardContent = getIntent().getStringExtra("content");
        if (cardContent != null) {
            tvContent.setText(cardContent);
        }
    }

    private void loadRecommendedCiPais() {
        // 获取传递的长度信息
        // 根据内容分析长度并请求匹配的词牌
        
        Log.d("CiPaiRecommendActivity", "loadRecommendedCiPais called");
        
        if (cardContent == null || cardContent.isEmpty()) {
            Toast.makeText(CiPaiRecommendActivity.this, "内容为空，无法推荐词牌", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d("CiPaiRecommendActivity", "cardContent: " + cardContent);
        
        // 构造请求数据
        List<List<Integer>> lengths = analyzeSentenceLengths(cardContent);
        
        Log.d("CiPaiRecommendActivity", "analyzed lengths: " + lengths);
        
        // 使用本地BackendService进行词牌匹配
        // 传递完整的二维列表
        try {
            // 将二维列表展平为一维列表
            List<Integer> flatLengths = new ArrayList<>();
            for (List<Integer> sublist : lengths) {
                flatLengths.addAll(sublist);
            }
            
            BackendService.Result<List<BackendService.CiPai>> result = backendService.matchCiPaiByLengths(flatLengths);
            
            Log.d("CiPaiRecommendActivity", "matchCiPaiByLengths result code: " + result.getCode());
            
            if (result.getCode() == 200 && result.getData() != null) {
                Log.d("CiPaiRecommendActivity", "matchCiPaiByLengths result data size: " + result.getData().size());
                
                ciPaiList.clear();
                // 将BackendService.CiPai转换为model.CiPai
                for (BackendService.CiPai backendCiPai : result.getData()) {
                    CiPai modelCiPai = new CiPai();
                    modelCiPai.setId(backendCiPai.getId());
                    modelCiPai.setName(backendCiPai.getName());
                    modelCiPai.setExampleText(backendCiPai.getExampleText());
                    modelCiPai.setSentenceLengths(backendCiPai.getSentenceLengths());
                    ciPaiList.add(modelCiPai);
                }
                ciPaiAdapter.notifyDataSetChanged();
            } else {
                Log.d("CiPaiRecommendActivity", "Failed to match ci pai: " + result.getMessage());
                Toast.makeText(CiPaiRecommendActivity.this, "未找到匹配的词牌: " + result.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("CiPaiRecommendActivity", "Exception in matchCiPaiByLengths", e);
            Toast.makeText(CiPaiRecommendActivity.this, "匹配词牌时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 分析文本内容，根据句号分割句子并计算每句的字符长度
     * @param content 输入的文本内容
     * @return 句子长度的二维列表，例如[[7, 6],[7],[5, 5]]
     */
    private List<List<Integer>> analyzeSentenceLengths(String content) {
        List<List<Integer>> allLengths = new ArrayList<>();
        if (content != null && !content.isEmpty()) {
            // 按句号、问号、感叹号、分号分割成句子组
            String[] sentenceGroups = content.split("[。？！；]");
            for (String group : sentenceGroups) {
                if (!group.trim().isEmpty()) {
                    // 对每个句子组，按逗号和顿号进一步分割
                    String[] phrases = group.split("[，、]");
                    List<Integer> lengths = new ArrayList<>();
                    for (String phrase : phrases) {
                        if (!phrase.trim().isEmpty()) {
                            lengths.add(phrase.trim().length());
                        }
                    }
                    // 只有当lengths不为空时才添加
                    if (!lengths.isEmpty()) {
                        allLengths.add(lengths);
                    }
                }
            }
        }
        return allLengths;
    }
}