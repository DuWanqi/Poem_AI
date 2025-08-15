package com.example.poemai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.poemai.utils.PreferencesManager;

public class LaunchActivity extends AppCompatActivity {
    private static final String TAG = "LaunchActivity";
    private Button btnLogin, btnRegister;
    private TextView tvAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        
        PreferencesManager preferencesManager = new PreferencesManager(this);
        String token = preferencesManager.getToken();
        
        // 添加调试日志
        Log.d(TAG, "onCreate: token = " + token);
        
        // 检查用户是否已经登录
        if (token != null) {
            // 用户已登录，直接跳转到主页面
            Log.d(TAG, "onCreate: 用户已登录，跳转到主页面");
            startActivity(new Intent(LaunchActivity.this, MainActivity.class));
            finish();
            return;
        }
        
        Log.d(TAG, "onCreate: 用户未登录，显示登录界面");
        initViews();
        setupListeners();
        setupAnimations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回LaunchActivity时都检查登录状态
        PreferencesManager preferencesManager = new PreferencesManager(this);
        String token = preferencesManager.getToken();
        
        // 添加调试日志
        Log.d(TAG, "onResume: token = " + token);
        
        if (token != null) {
            // 用户已登录，直接跳转到主页面
            Log.d(TAG, "onResume: 用户已登录，跳转到主页面");
            startActivity(new Intent(LaunchActivity.this, MainActivity.class));
            finish();
        }
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