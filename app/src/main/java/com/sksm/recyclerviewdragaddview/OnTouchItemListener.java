package com.sksm.recyclerviewdragaddview;

import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface OnTouchItemListener {
    boolean onTouchItem(int position, RecyclerView.Adapter adapter, View v, MotionEvent event);
}
