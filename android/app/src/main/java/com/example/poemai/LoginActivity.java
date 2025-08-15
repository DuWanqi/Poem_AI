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

import com.example.poemai.model.LoginRequest;
import com.example.poemai.model.LoginResponse;
import com.example.poemai.network.RetrofitClient;
import com.example.poemai.utils.PreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferencesManager = new PreferencesManager(this);
        
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
        
        LoginRequest request = new LoginRequest(username, password);
        Call<LoginResponse> call = RetrofitClient.getInstance().getApiService().login(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    
                    if (loginResponse.getToken() != null) {
                        // 登录成功
                        Log.d(TAG, "登录成功，token = " + loginResponse.getToken());
                        preferencesManager.saveAuthToken(loginResponse.getToken(), loginResponse.getUserId());
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // 登录失败
                        Log.d(TAG, "登录失败: " + loginResponse.getError());
                        Toast.makeText(LoginActivity.this, "登录失败: " + loginResponse.getError(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "登录失败，请检查用户名和密码");
                    Toast.makeText(LoginActivity.this, "登录失败，请检查用户名和密码", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                Log.e(TAG, "网络错误", t);
                Toast.makeText(LoginActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}