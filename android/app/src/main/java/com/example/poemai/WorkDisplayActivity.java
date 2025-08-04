package com.example.poemai;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.poemai.utils.PreferencesManager;
import com.example.poemai.network.RetrofitClient;
import com.google.gson.Gson;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkDisplayActivity extends AppCompatActivity {
    private TextView tvWorkTitle, tvWorkContent;
    private ImageButton btnMore;
    private PreferencesManager preferencesManager;
    private Map<String, Object> workData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_display);

        preferencesManager = new PreferencesManager(this);
        
        initViews();
        loadWorkData();
        setupListeners();
    }

    private void initViews() {
        tvWorkTitle = findViewById(R.id.tvWorkTitle);
        tvWorkContent = findViewById(R.id.tvWorkContent);
        btnMore = findViewById(R.id.btnMore);
    }

    private void loadWorkData() {
        // 通过JSON字符串传递数据
        String workDataJson = getIntent().getStringExtra("work_data_json");
        if (workDataJson != null) {
            Gson gson = new Gson();
            workData = gson.fromJson(workDataJson, Map.class);
            
            if (workData != null) {
                String title = (String) workData.get("title");
                String content = (String) workData.get("content");
                
                tvWorkTitle.setText(title != null ? title : "未命名作品");
                tvWorkContent.setText(content != null ? content : "无内容");
            }
        }
    }

    private void setupListeners() {
        btnMore.setOnClickListener(v -> showMoreOptions());
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void showMoreOptions() {
        String[] options = {"编辑", "生成", "删除"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("更多操作")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // 编辑
                            editWork();
                            break;
                        case 1: // 生成
                            generateWork();
                            break;
                        case 2: // 删除
                            deleteWork();
                            break;
                    }
                })
                .show();
    }

    private void editWork() {
        if (workData != null) {
            String workType = (String) workData.get("workType");
            if ("raw_card".equals(workType)) {
                // 编辑原始卡片，进入卡片创作界面
                Intent intent = new Intent(WorkDisplayActivity.this, CardCreateActivity.class);
                Gson gson = new Gson();
                String workDataJson = gson.toJson(workData);
                intent.putExtra("work_data_json", workDataJson);
                startActivity(intent);
            } else {
                // 其他类型作品的编辑逻辑
                Toast.makeText(this, "该类型作品暂不支持编辑", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateWork() {
        // 显示分享对话框（即卡片生成页面）
        showShareDialog();
    }

    private void showShareDialog() {
        Dialog shareDialog = new Dialog(this);
        shareDialog.setContentView(R.layout.dialog_share);
        
        // 初始化分享对话框中的控件
        TextView tvSharePreview = shareDialog.findViewById(R.id.tvSharePreview);
        View btnSelectBackground = shareDialog.findViewById(R.id.btnSelectBackground);
        View btnSaveToLocal = shareDialog.findViewById(R.id.btnSaveToLocal);
        View btnSaveToWorks = shareDialog.findViewById(R.id.btnSaveToWorks);
        View btnCloseShare = shareDialog.findViewById(R.id.btnCloseShare);
        
        // 设置预览内容
        if (workData != null) {
            String content = (String) workData.get("content");
            tvSharePreview.setText(content != null ? content : "无内容");
        }
        
        // 设置按钮点击事件
        btnSelectBackground.setOnClickListener(v -> {
            // 打开背景选择对话框
            showBackgroundSelectDialog();
        });
        
        btnSaveToLocal.setOnClickListener(v -> {
            // 保存到本地
            Toast.makeText(this, "保存到本地", Toast.LENGTH_SHORT).show();
        });
        
        btnSaveToWorks.setOnClickListener(v -> {
            // 保存到我的作品
            Toast.makeText(this, "保存到我的作品", Toast.LENGTH_SHORT).show();
        });
        
        btnCloseShare.setOnClickListener(v -> {
            shareDialog.dismiss();
        });
        
        shareDialog.show();
    }

    private void showBackgroundSelectDialog() {
        Dialog backgroundDialog = new Dialog(this);
        backgroundDialog.setContentView(R.layout.dialog_background_select);
        
        // 初始化背景选择对话框中的控件
        View btnElegant = backgroundDialog.findViewById(R.id.btnElegant);
        View btnHeroic = backgroundDialog.findViewById(R.id.btnHeroic);
        View btnBlank = backgroundDialog.findViewById(R.id.btnBlank);
        View btnRedSolid = backgroundDialog.findViewById(R.id.btnRedSolid);
        View btnDarkSolid = backgroundDialog.findViewById(R.id.btnDarkSolid);
        View btnImportBackground = backgroundDialog.findViewById(R.id.btnImportBackground);
        View btnCloseBackground = backgroundDialog.findViewById(R.id.btnCloseBackground);
        
        // 设置按钮点击事件
        btnElegant.setOnClickListener(v -> {
            Toast.makeText(this, "选择婉约风格背景", Toast.LENGTH_SHORT).show();
        });
        
        btnHeroic.setOnClickListener(v -> {
            Toast.makeText(this, "选择豪放风格背景", Toast.LENGTH_SHORT).show();
        });
        
        btnBlank.setOnClickListener(v -> {
            Toast.makeText(this, "选择空白风格背景", Toast.LENGTH_SHORT).show();
        });
        
        btnRedSolid.setOnClickListener(v -> {
            Toast.makeText(this, "选择朱红纯色背景", Toast.LENGTH_SHORT).show();
        });
        
        btnDarkSolid.setOnClickListener(v -> {
            Toast.makeText(this, "选择淡黑纯色背景", Toast.LENGTH_SHORT).show();
        });
        
        btnImportBackground.setOnClickListener(v -> {
            Toast.makeText(this, "导入外部图片背景", Toast.LENGTH_SHORT).show();
        });
        
        btnCloseBackground.setOnClickListener(v -> {
            backgroundDialog.dismiss();
        });
        
        backgroundDialog.show();
    }

    private void deleteWork() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除")
                .setMessage("确定要删除这个作品吗？")
                .setPositiveButton("是", (dialog, which) -> performDelete())
                .setNegativeButton("否", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performDelete() {
        if (workData != null) {
            String token = preferencesManager.getToken();
            if (token == null) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }

            Double workIdDouble = (Double) workData.get("id");
            if (workIdDouble == null) {
                Toast.makeText(this, "作品ID无效", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Long workId = workIdDouble.longValue();

            Call<Void> call = RetrofitClient.getInstance().getApiService().deleteWork("Bearer " + token, workId);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(WorkDisplayActivity.this, "作品删除成功", Toast.LENGTH_SHORT).show();
                        finish(); // 返回上一页
                    } else {
                        Toast.makeText(WorkDisplayActivity.this, "删除失败: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(WorkDisplayActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}