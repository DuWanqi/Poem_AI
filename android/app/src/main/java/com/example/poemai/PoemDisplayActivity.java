package com.example.poemai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.poemai.model.CiPai;

public class PoemDisplayActivity extends AppCompatActivity {
    private TextView tvCiPaiName, tvExamplePoem;
    private Button btnUseTemplate, btnBack;
    private CiPai selectedCiPai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poem_display);

        initViews();
        loadCiPaiData();
        setupListeners();
    }

    private void initViews() {
        tvCiPaiName = findViewById(R.id.tvCiPaiName);
        tvExamplePoem = findViewById(R.id.tvExamplePoem);
        btnUseTemplate = findViewById(R.id.btnUseTemplate);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadCiPaiData() {
        // 从Intent获取传递的词牌数据
        selectedCiPai = (CiPai) getIntent().getSerializableExtra("selected_cipai");
        
        if (selectedCiPai != null) {
            tvCiPaiName.setText(selectedCiPai.getName());
            tvExamplePoem.setText(selectedCiPai.getExampleText());
        }
    }

    private void setupListeners() {
        btnUseTemplate.setOnClickListener(v -> {
            // 选择该词牌为模板，进入诗词创作页面
            Intent intent = new Intent(PoemDisplayActivity.this, PoemComposeActivity.class);
            intent.putExtra("selected_cipai", selectedCiPai);
            
            // 如果有卡片内容也一并传递
            String cardContent = getIntent().getStringExtra("card_content");
            if (cardContent != null) {
                intent.putExtra("card_content", cardContent);
            }
            
            startActivity(intent);
        });
        
        btnBack.setOnClickListener(v -> finish());
    }
}