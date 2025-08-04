package com.example.poemai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poemai.model.CiPai;
import com.example.poemai.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CiPaiRecommendActivity extends AppCompatActivity {
    private TextView tvContent;
    private RecyclerView rvCiPaiList;
    private Button btnBack;
    private CiPaiAdapter ciPaiAdapter;
    private List<CiPai> ciPaiList;
    private String cardContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cipai_recommend);

        initViews();
        setupRecyclerView();
        loadContent();
        loadRecommendedCiPais();
    }

    private void initViews() {
        tvContent = findViewById(R.id.tvContent);
        rvCiPaiList = findViewById(R.id.rvCiPaiList);
        btnBack = findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        ciPaiList = new ArrayList<>();
        ciPaiAdapter = new CiPaiAdapter(ciPaiList, ciPai -> {
            // 点击词牌后进入推荐诗词展示界面
            Intent intent = new Intent(CiPaiRecommendActivity.this, PoemDisplayActivity.class);
            intent.putExtra("selected_cipai", ciPai);
            intent.putExtra("card_content", cardContent);
            startActivity(intent);
        });
        
        rvCiPaiList.setLayoutManager(new LinearLayoutManager(this));
        rvCiPaiList.setAdapter(ciPaiAdapter);
    }

    private void loadContent() {
        cardContent = getIntent().getStringExtra("content");
        if (cardContent != null) {
            tvContent.setText(cardContent);
        }
    }

    private void loadRecommendedCiPais() {
        // 获取传递的长度信息
        // 这里简化处理，实际应该根据内容分析长度并请求匹配的词牌
        
        // 构造请求数据
        Map<String, Object> requestBody = new HashMap<>();
        List<List<Integer>> lengths = new ArrayList<>();
        // 添加示例数据
        List<Integer> line1 = new ArrayList<>();
        line1.add(5);
        line1.add(5);
        lengths.add(line1);
        requestBody.put("lengths", lengths);
        
        Call<List<CiPai>> call = RetrofitClient.getInstance().getApiService().matchCiPai(requestBody);
        call.enqueue(new Callback<List<CiPai>>() {
            @Override
            public void onResponse(Call<List<CiPai>> call, Response<List<CiPai>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ciPaiList.clear();
                    ciPaiList.addAll(response.body());
                    ciPaiAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CiPaiRecommendActivity.this, "未找到匹配的词牌", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CiPai>> call, Throwable t) {
                Toast.makeText(CiPaiRecommendActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}