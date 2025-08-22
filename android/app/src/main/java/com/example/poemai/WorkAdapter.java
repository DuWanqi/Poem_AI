package com.example.poemai;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.WorkViewHolder> {
    private List<Map<String, Object>> worksList;

    public WorkAdapter(List<Map<String, Object>> worksList) {
        this.worksList = worksList;
    }

    @NonNull
    @Override
    public WorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_work, parent, false);
        return new WorkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkViewHolder holder, int position) {
        Map<String, Object> work = worksList.get(position);
        holder.bind(work);
    }

    @Override
    public int getItemCount() {
        return worksList.size();
    }

    class WorkViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvType, tvDate, tvContentPreview;

        public WorkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
//            tvType = itemView.findViewById(R.id.tvType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvContentPreview = itemView.findViewById(R.id.tvContentPreview);
        }

        public void bind(Map<String, Object> work) {
            // 从Map中获取作品信息并显示
            String title = (String) work.get("title");
//            String type = (String) work.get("workType");
            String date = (String) work.get("updatedAt");
            String content = (String) work.get("content");

            // 显示作品预览内容（前几行文字）
            if (content != null && !content.isEmpty()) {
                // 提取内容的前50个字符作为预览
                String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
                tvContentPreview.setText(preview);
            } else {
                tvContentPreview.setText("暂无内容预览");
            }

            tvTitle.setText(title != null ? title : "未命名作品");
//            tvType.setText(type != null ? type : "未知类型");
            
            // 简化日期显示
            if (date != null && date.length() > 10) {
                tvDate.setText(date.substring(0, 10));
            } else {
                tvDate.setText(date);
            }
            
            // 添加点击事件处理
            itemView.setOnClickListener(v -> {
                // 点击作品项，进入作品展示页面
                Intent intent = new Intent(itemView.getContext(), WorkDisplayActivity.class);
                Gson gson = new Gson();
                String workDataJson = gson.toJson(work);
                intent.putExtra("work_data_json", workDataJson);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}