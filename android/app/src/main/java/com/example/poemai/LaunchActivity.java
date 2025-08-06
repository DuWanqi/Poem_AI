package com.example.poemai;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.poemai.utils.PreferencesManager;

public class LaunchActivity extends AppCompatActivity {
    private Button btnLogin, btnRegister;
    private TextView tvAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        
        PreferencesManager preferencesManager = new PreferencesManager(this);
        
        // 检查用户是否已经登录
        if (preferencesManager.getToken() != null) {
            // 用户已登录，直接跳转到主页面
            startActivity(new Intent(LaunchActivity.this, MainActivity.class));
            finish();
            return;
        }
        
        initViews();
        setupListeners();
        setupAnimations();
    }

    private void initViews() {
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvAppName = findViewById(R.id.tvAppName);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            try {
                startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
            } catch (Exception e) {
                Toast.makeText(LaunchActivity.this, "无法启动登录界面: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        
        btnRegister.setOnClickListener(v -> {
            try {
                startActivity(new Intent(LaunchActivity.this, RegisterActivity.class));
            } catch (Exception e) {
                Toast.makeText(LaunchActivity.this, "无法启动注册界面: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void setupAnimations() {
        // 淡入动画
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvAppName.startAnimation(fadeIn);
        
        // 滑入动画
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        btnLogin.startAnimation(slideIn);
        
        // 稍微延迟注册按钮的动画
        slideIn.setStartOffset(200);
        btnRegister.startAnimation(slideIn);
    }
}