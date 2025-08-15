package com.example.poemai;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.poemai.model.WorkSaveResponse;
import com.example.poemai.network.RetrofitClient;
import com.example.poemai.utils.PreferencesManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardGenerateActivity extends AppCompatActivity {
    private TextView tvCardPreview;
    private Button btnSelectBackground, btnSaveToLocal, btnSaveToWorks, btnBack;
    private SharedPreferences settingsPrefs;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_generate);
        
        settingsPrefs = getSharedPreferences("PoemComposeSettings", MODE_PRIVATE);
        preferencesManager = new PreferencesManager(this);
        
        initViews();
        setupListeners();
        loadPreviewContent();
    }
    
    private void initViews() {
        tvCardPreview = findViewById(R.id.tvCardPreview);
        btnSelectBackground = findViewById(R.id.btnSelectBackground);
        btnSaveToLocal = findViewById(R.id.btnSaveToLocal);
        btnSaveToWorks = findViewById(R.id.btnSaveToWorks);
        btnBack = findViewById(R.id.btnBack);
    }
    
    private void setupListeners() {
        btnSelectBackground.setOnClickListener(v -> showBackgroundSelectDialog());
        btnSaveToLocal.setOnClickListener(v -> Toast.makeText(this, "保存到本地功能待实现", Toast.LENGTH_SHORT).show());
        btnSaveToWorks.setOnClickListener(v -> saveWorkToMyWorks());
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void loadPreviewContent() {
        // 获取传递的内容
        String content = getIntent().getStringExtra("content");
        if (content != null) {
            tvCardPreview.setText(content);
        }
        
        // 应用保存的背景
        applySelectedBackground();
    }
    
    private void applySelectedBackground() {
        String selectedBackground = settingsPrefs.getString("selected_background", "blank");
        switch (selectedBackground) {
            case "elegant":
                tvCardPreview.setBackgroundResource(R.drawable.elegant_background);
                break;
            case "heroic":
                tvCardPreview.setBackgroundResource(R.drawable.heroic_background);
                break;
            case "blank":
                tvCardPreview.setBackgroundResource(R.drawable.blank_background);
                break;
            case "red_solid":
                tvCardPreview.setBackgroundResource(R.drawable.red_solid_background);
                break;
            case "dark_solid":
                tvCardPreview.setBackgroundResource(R.drawable.dark_solid_background);
                break;
        }
    }
    
    private void showBackgroundSelectDialog() {
        Dialog backgroundDialog = new Dialog(this);
        backgroundDialog.setContentView(R.layout.dialog_background_select);
        
        // 初始化控件
        Button btnElegant = backgroundDialog.findViewById(R.id.btnElegant);
        Button btnHeroic = backgroundDialog.findViewById(R.id.btnHeroic);
        Button btnBlank = backgroundDialog.findViewById(R.id.btnBlank);
        Button btnRedSolid = backgroundDialog.findViewById(R.id.btnRedSolid);
        Button btnDarkSolid = backgroundDialog.findViewById(R.id.btnDarkSolid);
        Button btnImportBackground = backgroundDialog.findViewById(R.id.btnImportBackground);
        Button btnCloseBackground = backgroundDialog.findViewById(R.id.btnCloseBackground);
        
        // 设置按钮点击事件
        btnElegant.setOnClickListener(v -> {
            Toast.makeText(this, "已选择婉约风格背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "elegant");
            editor.apply();
            applySelectedBackground();
            backgroundDialog.dismiss();
        });
        
        btnHeroic.setOnClickListener(v -> {
            Toast.makeText(this, "已选择豪放风格背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "heroic");
            editor.apply();
            applySelectedBackground();
            backgroundDialog.dismiss();
        });
        
        btnBlank.setOnClickListener(v -> {
            Toast.makeText(this, "已选择空白风格背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "blank");
            editor.apply();
            applySelectedBackground();
            backgroundDialog.dismiss();
        });
        
        btnRedSolid.setOnClickListener(v -> {
            Toast.makeText(this, "已选择朱红纯色背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "red_solid");
            editor.apply();
            applySelectedBackground();
            backgroundDialog.dismiss();
        });
        
        btnDarkSolid.setOnClickListener(v -> {
            Toast.makeText(this, "已选择淡黑纯色背景", Toast.LENGTH_SHORT).show();
            // 保存选择的背景类型
            SharedPreferences.Editor editor = settingsPrefs.edit();
            editor.putString("selected_background", "dark_solid");
            editor.apply();
            applySelectedBackground();
            backgroundDialog.dismiss();
        });
        
        btnImportBackground.setOnClickListener(v -> {
            Toast.makeText(this, "导入外部图片功能待实现", Toast.LENGTH_SHORT).show();
            backgroundDialog.dismiss();
        });
        
        btnCloseBackground.setOnClickListener(v -> backgroundDialog.dismiss());
        
        backgroundDialog.show();
    }
    
    private void saveWorkToMyWorks() {
        // 获取预览内容
        String content = tvCardPreview.getText().toString();
        
        // 创建作品数据
        Map<String, Object> workData = new HashMap<>();
        workData.put("title", "我的作品");
        workData.put("content", content);
        workData.put("workType", "template_poem");
        
        // 添加字体设置
        Map<String, Object> fontSetting = new HashMap<>();
        fontSetting.put("font", "默认");
        fontSetting.put("size", 16);
        fontSetting.put("color", "#000000");
        workData.put("fontSetting", fontSetting);
        
        // 添加背景信息 (从SharedPreferences获取)
        Map<String, Object> backgroundInfo = new HashMap<>();
        String selectedBackground = settingsPrefs.getString("selected_background", "blank");
        backgroundInfo.put("type", "drawable");
        backgroundInfo.put("value", selectedBackground);
        workData.put("backgroundInfo", backgroundInfo);
        
        // 获取用户token
        String token = preferencesManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "用户未登录，请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 调用API保存作品
        Call<WorkSaveResponse> call = RetrofitClient.getInstance().getApiService().saveWork("Bearer " + token, workData);
        call.enqueue(new Callback<WorkSaveResponse>() {
            @Override
            public void onResponse(Call<WorkSaveResponse> call, retrofit2.Response<WorkSaveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WorkSaveResponse result = response.body();
                    if (result.getCode() == 200 && result.getId() != null) {
                        Toast.makeText(CardGenerateActivity.this, "作品保存成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CardGenerateActivity.this, "作品保存失败: " + result.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CardGenerateActivity.this, "作品保存失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WorkSaveResponse> call, Throwable t) {
                Toast.makeText(CardGenerateActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}