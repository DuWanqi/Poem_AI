package com.example.poemai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.poemai.service.BackendService;
import com.example.poemai.utils.PreferencesManager;

import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private EditText etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private PreferencesManager preferencesManager;
    private BackendService backendService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        preferencesManager = new PreferencesManager(this);
        backendService = BackendService.getInstance(this);
        
        Log.d(TAG, "onCreate: token = " + preferencesManager.getToken());
        
        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> performRegister());
        
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void performRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        
        // 使用本地BackendService进行注册
        BackendService.Result<Map<String, Object>> result = backendService.register(username, password);
        
        btnRegister.setEnabled(true);
        
        if (result.getCode() == 200 && result.getData() != null) {
            Map<String, Object> data = result.getData();
            String token = (String) data.get("token");
            Long userId = (Long) data.get("userId");
            
            Log.d(TAG, "注册成功，token = " + token);
            preferencesManager.saveAuthToken(token, userId);
            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Log.d(TAG, "注册失败: " + result.getMessage());
            Toast.makeText(RegisterActivity.this, "注册失败: " + result.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}