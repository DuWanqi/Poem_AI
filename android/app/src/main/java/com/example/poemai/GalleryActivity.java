package com.example.poemai;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poemai.database.DatabaseHelper;
import com.example.poemai.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity {
    private RecyclerView rvWorks;
    private WorkAdapter workAdapter;
    private List<Map<String, Object>> worksList;
    private PreferencesManager preferencesManager;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        preferencesManager = new PreferencesManager(this);
        databaseHelper = new DatabaseHelper(this);
        initViews();
        loadWorks();
    }

    private void initViews() {
        rvWorks = findViewById(R.id.rvWorks);
        worksList = new ArrayList<>();
        workAdapter = new WorkAdapter(worksList);
        rvWorks.setLayoutManager(new LinearLayoutManager(this));
        rvWorks.setAdapter(workAdapter);
    }

    private void loadWorks() {
        long userId = preferencesManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        // 使用本地DatabaseHelper获取作品列表
        List<DatabaseHelper.Work> works = databaseHelper.getWorksByUserId(userId);
        worksList.clear();
        
        // 转换为适配器需要的格式
        for (DatabaseHelper.Work work : works) {
            Map<String, Object> workMap = new HashMap<>();
            workMap.put("id", work.getId());
            workMap.put("title", work.getTitle());
            workMap.put("content", work.getContent());
            workMap.put("workType", work.getWorkType());
            workMap.put("backgroundInfo", work.getBackgroundInfo());
            workMap.put("createdAt", work.getCreatedAt());
            workMap.put("updatedAt", work.getUpdatedAt());
            worksList.add(workMap);
        }
        
        workAdapter.notifyDataSetChanged();
    }
}