package com.example.poemai;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poemai.utils.PreferencesManager;
import com.example.poemai.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GalleryActivity extends AppCompatActivity {
    private RecyclerView rvWorks;
    private Button btnBack;
    private WorkAdapter workAdapter;
    private List<Map<String, Object>> worksList;
    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        preferencesManager = new PreferencesManager(this);
        
        initViews();
        setupRecyclerView();
        loadWorks();
    }

    private void initViews() {
        rvWorks = findViewById(R.id.rvWorks);
        btnBack = findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        worksList = new ArrayList<>();
        workAdapter = new WorkAdapter(worksList);
        
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvWorks.setLayoutManager(layoutManager);
        rvWorks.setAdapter(workAdapter);
    }

    private void loadWorks() {
        String token = preferencesManager.getToken();
        if (token == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 调用 API 获取作品列表
        Call<List<Map<String, Object>>> call = RetrofitClient.getInstance().getApiService().getAllWorks("Bearer " + token);
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    worksList.clear();
                    worksList.addAll(response.body());
                    workAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(GalleryActivity.this, "加载作品列表失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(GalleryActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}