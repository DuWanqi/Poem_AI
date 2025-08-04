package com.example.poemai;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.poemai.utils.PreferencesManager;
import com.example.poemai.network.RetrofitClient;
import com.example.poemai.model.LoginResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardCreateActivity extends AppCompatActivity {
    private EditText etCardContent;
    private Button btnCreatePoem, btnRecommendCiPai, btnSaveCard;
    private ImageButton btnSettings;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_create);

        preferencesManager = new PreferencesManager(this);
        
        initViews();
        setupListeners();
    }

    private void initViews() {
        etCardContent = findViewById(R.id.etCardContent);
        btnRecommendCiPai = findViewById(R.id.btnRecommendCiPai);
        btnSaveCard = findViewById(R.id.btnSaveCard);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void setupListeners() {
        // 移除了创建诗词按钮的监听器，因为该功能暂时未使用

        btnRecommendCiPai.setOnClickListener(v -> {
            String content = etCardContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入卡片内容", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 分析内容长度以推荐词牌
            List<List<Integer>> lengths = analyzeContentLengths(content);
            
            Intent intent = new Intent(CardCreateActivity.this, CiPaiRecommendActivity.class);
            intent.putExtra("content", content);
            
            // 传递长度信息
            intent.putExtra("lengths", new ArrayList<>(lengths)); // 简化处理
            startActivity(intent);
        });

        btnSaveCard.setOnClickListener(v -> saveCard());
        
        btnSettings.setOnClickListener(v -> showSettingsDialog());
    }

    private List<List<Integer>> analyzeContentLengths(String content) {
        List<List<Integer>> lengths = new ArrayList<>();
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                List<Integer> lineLengths = new ArrayList<>();
                lineLengths.add(line.trim().length());
                lengths.add(lineLengths);
            }
        }
        
        return lengths;
    }
    
    private void showSettingsDialog() {
        Dialog settingsDialog = new Dialog(this);
        settingsDialog.setContentView(R.layout.dialog_settings);
        
        // 初始化设置对话框中的控件
        Button btnFontSizeDecrease = settingsDialog.findViewById(R.id.btnFontSizeDecrease);
        Button btnFontSizeIncrease = settingsDialog.findViewById(R.id.btnFontSizeIncrease);
        Button btnImportFont = settingsDialog.findViewById(R.id.btnImportFont);
        Button btnSelectDirection = settingsDialog.findViewById(R.id.btnSelectDirection);
        Button btnCloseSettings = settingsDialog.findViewById(R.id.btnCloseSettings);
        
        // 设置按钮点击事件
        btnFontSizeDecrease.setOnClickListener(v -> {
            // 减小字体大小
            Toast.makeText(this, "减小字体大小", Toast.LENGTH_SHORT).show();
        });
        
        btnFontSizeIncrease.setOnClickListener(v -> {
            // 增大字体大小
            Toast.makeText(this, "增大字体大小", Toast.LENGTH_SHORT).show();
        });
        
        btnImportFont.setOnClickListener(v -> {
            // 导入字体
            showFontImportDialog();
        });
        
        btnSelectDirection.setOnClickListener(v -> {
            // 选择排列方向
            showDirectionSelectDialog();
        });
        
        btnCloseSettings.setOnClickListener(v -> {
            settingsDialog.dismiss();
        });
        
        settingsDialog.show();
    }
    
    private void showFontImportDialog() {
        Toast.makeText(this, "打开字体导入窗口", Toast.LENGTH_SHORT).show();
        // 这里应该实现字体导入功能
    }
    
    private void showDirectionSelectDialog() {
        Toast.makeText(this, "打开排列方向选择界面", Toast.LENGTH_SHORT).show();
        // 这里应该实现排列方向选择功能
    }

    private void saveCard() {
        String content = etCardContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入卡片内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构造作品数据
        Map<String, Object> workData = new HashMap<>();
        workData.put("title", "卡片作品");
        workData.put("content", content);
        workData.put("workType", "raw_card");

        // 获取 token
        String token = preferencesManager.getToken();
        if (token == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 调用 API 保存作品
        Call<Map<String, Object>> call = RetrofitClient.getInstance().getApiService().saveWork("Bearer " + token, workData);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CardCreateActivity.this, "卡片保存成功", Toast.LENGTH_SHORT).show();
                    // 可以在这里处理保存成功的逻辑，比如刷新界面等
                } else {
                    Toast.makeText(CardCreateActivity.this, "保存失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(CardCreateActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}