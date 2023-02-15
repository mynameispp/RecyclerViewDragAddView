package com.sksm.recyclerviewdragaddview;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sksm.recyclerviewdragaddview.databinding.ItemMainListBinding;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyHolder> {
    private List<String> data;
    private OnTouchItemListener onTouchItemListener;

    public MainAdapter(List<String> data, OnTouchItemListener onTouchItemListener) {
        this.onTouchItemListener = onTouchItemListener;
        this.data = data;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(ItemMainListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.setData(data.get(position));

        holder.itemView.setTag(position);
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (onTouchItemListener != null) {
                    int position = (int) v.getTag();
                    return onTouchItemListener.onTouchItem(position, MainAdapter.this, v, event);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    static class MyHolder extends RecyclerView.ViewHolder {
        private final ItemMainListBinding myViewBinding;

        public MyHolder(@NonNull ItemMainListBinding myViewBinding) {
            super(myViewBinding.getRoot());
            this.myViewBinding = myViewBinding;
        }

        private void setData(String data) {
            myViewBinding.itemTxt.setText(data);
        }
    }
}
