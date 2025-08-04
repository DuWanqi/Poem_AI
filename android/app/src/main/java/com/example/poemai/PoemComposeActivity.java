package com.example.poemai;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.poemai.utils.PreferencesManager;
import com.example.poemai.model.CiPai;

public class PoemComposeActivity extends AppCompatActivity {
    private TextView tvCardContent, tvPoemTemplate;
    private EditText etTitle, etAuthor;
    private ImageButton btnRhyme, btnShare, btnSettings, btnInspiration;
    private PreferencesManager preferencesManager;
    private CiPai selectedCiPai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poem_compose);

        preferencesManager = new PreferencesManager(this);
        
        initViews();
        loadCardContent();
        setupListeners();
    }

    private void initViews() {
        tvCardContent = findViewById(R.id.tvCardContent);
        tvPoemTemplate = findViewById(R.id.tvPoemTemplate);
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        
        btnRhyme = findViewById(R.id.btnRhyme);
        btnShare = findViewById(R.id.btnShare);
        btnSettings = findViewById(R.id.btnSettings);
        btnInspiration = findViewById(R.id.btnInspiration);
    }

    private void loadCardContent() {
        String cardContent = getIntent().getStringExtra("card_content");
        if (cardContent != null) {
            tvCardContent.setText(cardContent);
        }
        
        // 获取选中的词牌信息
        selectedCiPai = (CiPai) getIntent().getSerializableExtra("selected_cipai");
        if (selectedCiPai != null) {
            // 根据词牌生成模板
            generatePoemTemplate(selectedCiPai);
        }
    }

    private void generatePoemTemplate(CiPai ciPai) {
        // 这里应该根据词牌的格式生成下划线模板
        // 简化处理，仅作为示例
        StringBuilder template = new StringBuilder();
        template.append("请根据词牌《").append(ciPai.getName()).append("》的格式创作诗词\n\n");
        
        // 示例格式，实际应该根据词牌的sentenceLengths生成
        template.append("_______ _______ _______\n");
        template.append("_______ _______ _______\n");
        template.append("_______ _______\n");
        template.append("_______ _______\n\n");
        template.append("_______ _______ _______\n");
        template.append("_______ _______ _______\n");
        template.append("_______ _______\n");
        template.append("_______ _______\n");
        
        tvPoemTemplate.setText(template.toString());
    }

    private void setupListeners() {
        btnRhyme.setOnClickListener(v -> showRhymeDialog());
        btnShare.setOnClickListener(v -> showShareDialog());
        btnSettings.setOnClickListener(v -> showSettingsDialog());
        btnInspiration.setOnClickListener(v -> showInspirationDialog());
    }

    private void showRhymeDialog() {
        Toast.makeText(this, "打开推荐韵脚界面", Toast.LENGTH_SHORT).show();
        // TODO: 实现推荐韵脚界面
    }

    private void showShareDialog() {
        Toast.makeText(this, "打开分享界面", Toast.LENGTH_SHORT).show();
        // TODO: 实现分享界面
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
            Toast.makeText(this, "打开字体导入窗口", Toast.LENGTH_SHORT).show();
        });
        
        btnSelectDirection.setOnClickListener(v -> {
            // 选择排列方向
            Toast.makeText(this, "打开排列方向选择界面", Toast.LENGTH_SHORT).show();
        });
        
        btnCloseSettings.setOnClickListener(v -> {
            settingsDialog.dismiss();
        });
        
        settingsDialog.show();
    }

    private void showInspirationDialog() {
        Toast.makeText(this, "打开灵感（AI补全）界面", Toast.LENGTH_SHORT).show();
        // TODO: 实现AI补全界面
    }
}