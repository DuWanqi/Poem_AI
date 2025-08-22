package com.example.poemai;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.poemai.model.ApiResponse;
import java.util.Map;
import com.example.poemai.utils.PreferencesManager;
import com.example.poemai.service.BackendService;
import com.example.poemai.database.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CREATE_CARD = 1001;
    private PreferencesManager preferencesManager;
    private BackendService backendService; // 使用BackendService而不是Retrofit
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
        // 隐藏ActionBar标题，避免与自定义内容冲突
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        preferencesManager = new PreferencesManager(this);

//        // 设置Toolbar
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // 设置帮助按钮点击事件
        ImageButton btnHelp = findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDialog();
            }
        });

        try {
            backendService = BackendService.getInstance(this); // 初始化BackendService
        } catch (Exception e) {
            Log.e(TAG, "初始化BackendService失败", e);
            Toast.makeText(this, "初始化服务失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        
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

    private void loadWorks(String token) {
        // 使用本地BackendService检查token有效性
        // 在这个简化版本中，只要有token就认为有效
        // 实际应用中可以根据需要实现更复杂的验证逻辑
        if (token == null || token.isEmpty()) {
            // Token无效，清除本地存储
            preferencesManager.clearAuthToken();
        }
        // 如果有token，就认为有效，不需要做任何事
    }
    
    private void checkTokenValidity() {
        String token = preferencesManager.getToken();
        if (token != null && !token.isEmpty()) {
            // 验证token有效性
            loadWorks(token);
        } else {
            // Token不存在，跳转到登录页面
            startLoginActivity();
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LaunchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // 无参版本，供 onActivityResult 调用
    private void loadWorks() {
        String token = preferencesManager.getToken();
        if (token != null && !token.isEmpty()) {
            loadWorks(token);
        }
        
        // 使用BackendService获取作品列表
        if (backendService != null) {
            long userId = preferencesManager.getUserId();
            if (userId > 0) {
                try {
                    BackendService.Result<List<DatabaseHelper.Work>> result = backendService.getAllWorks(userId);
                    if (result.getCode() == 200 && result.getData() != null) {
                        // 清空现有数据
                        worksList.clear();
                        
                        // 将DatabaseHelper.Work对象转换为Map<String, Object>
                        for (DatabaseHelper.Work work : result.getData()) {
                            Map<String, Object> workMap = new HashMap<>();
                            workMap.put("id", work.getId());
                            workMap.put("title", work.getTitle());
                            workMap.put("content", work.getContent());
                            workMap.put("workType", work.getWorkType());
                            // 将Date类型转换为String类型，避免WorkAdapter中出现ClassCastException
                            workMap.put("createdAt", work.getCreatedAt() != null ? work.getCreatedAt().toString() : "");
                            workMap.put("updatedAt", work.getUpdatedAt() != null ? work.getUpdatedAt().toString() : "");
                            worksList.add(workMap);
                        }
                        
                        // 更新UI
                        runOnUiThread(() -> workAdapter.notifyDataSetChanged());
                    } else {
                        Log.e(TAG, "获取作品列表失败: " + result.getMessage());
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "获取作品列表失败: " + result.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取作品列表时发生异常", e);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "获取作品列表异常: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        }
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
            loadWorks(); // 使用无参方法刷新作品列表
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回主界面时都检查token有效性并刷新作品列表
        checkTokenValidity();
        loadWorks();
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.how_to_use);

        // 从res/raw文件夹读取帮助文本
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.help);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String helpText = new String(buffer, "UTF-8");

            builder.setMessage(helpText);
        } catch (IOException e) {
            e.printStackTrace();
            builder.setMessage("无法加载帮助内容");
        }

        builder.setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}