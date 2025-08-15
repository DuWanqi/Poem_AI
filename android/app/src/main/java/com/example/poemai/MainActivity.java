package com.example.poemai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.poemai.model.ApiResponse;
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
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CREATE_CARD = 1001;
    private PreferencesManager preferencesManager;
    private FloatingActionButton btnCreateCard;
    private RecyclerView rvWorks;
    private WorkAdapter workAdapter;
    private List<Map<String, Object>> worksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 添加Toolbar支持
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
            Intent intent = new Intent(MainActivity.this, CardCreateActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_CARD);
        });
    }

    private void loadWorks() {
        String token = preferencesManager.getToken();
        if (token == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 调用 API 获取作品列表
        Call<ApiResponse> call = RetrofitClient.getInstance().getApiService().getAllWorks("Bearer " + token);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    worksList.clear();
                    worksList.addAll(response.body().getData());
                    workAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "加载作品列表失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Log.d(TAG, "用户选择退出登录");
            // 清除保存的认证信息
            preferencesManager.clearAuthToken();
            // 跳转到启动页面
            Intent intent = new Intent(MainActivity.this, LaunchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 如果是从卡片创建页面返回，并且保存成功，则刷新作品列表
        if (requestCode == REQUEST_CREATE_CARD && resultCode == RESULT_OK) {
            loadWorks();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回主界面时都刷新作品列表
        loadWorks();
    }
}