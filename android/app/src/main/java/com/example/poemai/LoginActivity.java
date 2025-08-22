package com.example.poemai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.poemai.service.BackendService;
import com.example.poemai.utils.PreferencesManager;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private PreferencesManager preferencesManager;
    private BackendService backendService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferencesManager = new PreferencesManager(this);
        backendService = BackendService.getInstance(this);
        
        Log.d(TAG, "onCreate: token = " + preferencesManager.getToken());
        
        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        
        // 使用本地BackendService进行登录
        BackendService.Result<Map<String, Object>> result = backendService.login(username, password);
        
        btnLogin.setEnabled(true);
        
        if (result.getCode() == 200 && result.getData() != null) {
            Map<String, Object> data = result.getData();
            String token = (String) data.get("token");
            Long userId = (Long) data.get("userId");
            
            Log.d(TAG, "登录成功，token = " + token);
            preferencesManager.saveAuthToken(token, userId);
            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Log.d(TAG, "登录失败: " + result.getMessage());
            Toast.makeText(LoginActivity.this, "登录失败: " + result.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}