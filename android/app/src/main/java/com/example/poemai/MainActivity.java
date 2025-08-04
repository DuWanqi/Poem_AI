package com.example.poemai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.poemai.utils.PreferencesManager;
import com.example.poemai.network.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private PreferencesManager preferencesManager;
    private FloatingActionButton btnCreateCard;
    private RecyclerView rvWorks;
    private WorkAdapter workAdapter;
    private List<Map<String, Object>> worksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferencesManager = new PreferencesManager(this);
        
        initViews();
        setupRecyclerView();
        setupListeners();
        loadWorks();
    }

    private void initViews() {
        btnCreateCard = findViewById(R.id.btnCreateCard);
        rvWorks = findViewById(R.id.rvWorks);
        worksList = new ArrayList<>();
        workAdapter = new WorkAdapter(worksList);
    }

    private void setupRecyclerView() {
        // 使用瀑布流布局管理器
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvWorks.setLayoutManager(layoutManager);
        rvWorks.setAdapter(workAdapter);
    }

    private void setupListeners() {
        btnCreateCard.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CardCreateActivity.class));
        });
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
                    Toast.makeText(MainActivity.this, "加载作品列表失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}