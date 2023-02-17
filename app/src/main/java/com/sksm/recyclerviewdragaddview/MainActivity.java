package com.sksm.recyclerviewdragaddview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.sksm.recyclerviewdragaddview.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnTouchItemListener{
    private ActivityMainBinding myViewBinding;
    private MainLinearManager manager;
    private MainAdapter mainAdapter;
    /**
     * 上一次点击的坐标
     */
    private float lastX;
    private float lastY;
    /**
     * 是否移动
     */
    private boolean isMove;

    private ImageView touchView;
    private boolean isScroll = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1054) {
                //复制Item并添加一个拖动的ImageView（touchView）
                manager.setScrollEnabled(false);
                myViewBinding.getRoot().addView(touchView);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐去电池等图标和一切修饰部分（状态栏部分）
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题全屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myViewBinding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(myViewBinding.getRoot());
        initView();
    }

    public Bitmap view2bitmap(View view) {
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

    private void initView() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            strings.add("长按拖动添加 " + i);
        }
        mainAdapter = new MainAdapter(strings,this);
        manager = new MainLinearManager(this);
        myViewBinding.myRv.setLayoutManager(manager);
        myViewBinding.myRv.setAdapter(mainAdapter);
        myViewBinding.myRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                isMove = true;
                isScroll = false;
                mHandler.removeCallbacks(runnable);
            }
        });
    }

    @Override
    public boolean onTouchItem(int position, RecyclerView.Adapter adapter, View v, MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        int[] viewPosition = new int[2];
        v.getLocationInWindow(viewPosition);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isScroll) {
                    isMove = false;
                    lastX = x;
                    lastY = y;
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(v.getWidth(), v.getHeight());
                    params.topMargin = viewPosition[1];
                    params.leftMargin = viewPosition[0];
                    touchView = new ImageView(MainActivity.this);
                    touchView.setLayoutParams(params);
                    touchView.setImageBitmap(view2bitmap(v));
                    touchView.setBackgroundColor(getResources().getColor(R.color.purple_500));
                    mHandler.postDelayed(runnable, ViewConfiguration.getLongPressTimeout());
                    isScroll = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMove && !manager.isScrollEnabled()) {
                    int moveX = (int) (x - lastX);
                    int moveY = (int) (y - lastY);
                    Log.i(MainActivity.class.getName(), "ACTION_MOVE");
                    FrameLayout.LayoutParams moveParams = (FrameLayout.LayoutParams) touchView.getLayoutParams();
                    moveParams.topMargin = viewPosition[1] + moveY;
                    moveParams.leftMargin = viewPosition[0] + moveX;
                    touchView.setLayoutParams(moveParams);

                    if (myViewBinding.myRv.getVisibility()==View.VISIBLE) {
                        myViewBinding.myRv.setVisibility(View.GONE);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isScroll) {
                    isScroll = false;
                    if (touchView != null) {
                        if (!manager.isScrollEnabled()) {
                            myViewBinding.getRoot().removeView(touchView);
                        }
                        if (canAddViewToLayout(touchView)) {
                            //设置touchView在relativeLayout里面的位置
                            FrameLayout.LayoutParams moveParams = (FrameLayout.LayoutParams) touchView.getLayoutParams();
                            //判断是超出X轴的范围
                            if ((touchView.getLeft() + touchView.getWidth()) > myViewBinding.myAddView.getRight()) {
                                moveParams.leftMargin = (myViewBinding.myAddView.getRight() - touchView.getWidth());
                            } else if (touchView.getLeft() < (myViewBinding.myAddView.getLeft() + myViewBinding.myRv.getWidth())) {
                                moveParams.leftMargin = myViewBinding.myAddView.getLeft() + myViewBinding.myRv.getWidth();
                            }
                            //判断是否超出Y轴的范围
                            if ((touchView.getTop() + touchView.getHeight()) > myViewBinding.myAddView.getBottom()) {
                                moveParams.topMargin = (myViewBinding.myAddView.getBottom() - touchView.getHeight());
                            } else if (touchView.getTop() < myViewBinding.myAddView.getTop()) {
                                moveParams.topMargin = 0;
                            }
                            touchView.setLayoutParams(moveParams);
                            //添加View
                            myViewBinding.myAddView.addView(touchView);
                        }
                        touchView = null;
                    }
                }
                myViewBinding.myRv.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(runnable);
                manager.setScrollEnabled(true);
                break;
        }
        return true;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Message message = Message.obtain();
            message.what = 1054;
            mHandler.sendMessage(message);

        }
    };

    //是否在要加入的布局范围内
    public boolean canAddViewToLayout(View view) {
        float x = view.getX();
        float y = view.getY();
        int left = myViewBinding.myAddView.getLeft() + myViewBinding.myRv.getWidth();//因为myViewBinding.myAddView是全屏，需要将myRv遮盖的范围排除不添加View
        int right = myViewBinding.myAddView.getRight();
        int top = myViewBinding.myAddView.getTop();
        int bottom = myViewBinding.myAddView.getBottom();
        Log.e("ttttttttttt", "x=" + x + ",y=" + y + ",left=" + left + ",right=" + right + ",top=" + top + ",bottom=" + bottom);
        if ((x + view.getHeight()) > left && x < right && (y + view.getHeight()) >= top && y < bottom) {
            return true;
        } else {
            return false;
        }
    }
}