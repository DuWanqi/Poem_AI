package com.example.poemai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poemai.model.CiPai;

import java.util.List;

public class CiPaiAdapter extends RecyclerView.Adapter<CiPaiAdapter.CiPaiViewHolder> {
    private List<CiPai> ciPaiList;
    private OnCiPaiClickListener listener;

    public CiPaiAdapter(List<CiPai> ciPaiList, OnCiPaiClickListener listener) {
        this.ciPaiList = ciPaiList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CiPaiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cipai, parent, false);
        return new CiPaiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CiPaiViewHolder holder, int position) {
        CiPai ciPai = ciPaiList.get(position);
        holder.bind(ciPai);
    }

    @Override
    public int getItemCount() {
        return ciPaiList.size();
    }

    class CiPaiViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvExample;

        public CiPaiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvExample = itemView.findViewById(R.id.tvExample);
        }

        public void bind(CiPai ciPai) {
            tvName.setText(ciPai.getName());
            tvExample.setText(ciPai.getExampleText());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCiPaiClick(ciPai);
                }
            });
        }
    }

    public interface OnCiPaiClickListener {
        void onCiPaiClick(CiPai ciPai);
    }
}