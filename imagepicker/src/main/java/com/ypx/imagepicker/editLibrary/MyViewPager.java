package com.ypx.imagepicker.editLibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

/**
 * time：2021-09-06
 * author：pachy1990
 * 描述：
 */
public class MyViewPager extends ViewPager {
    private boolean isCanScroll = true;


    public MyViewPager(@NonNull Context context) {
        super(context);
    }
    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isCanScroll) {
            //允许滑动则应该调用父类的方法
            return super.onTouchEvent(ev);
        } else {
            //禁止滑动则不做任何操作，直接返回true即可
            return true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (isCanScroll)
            return super.onInterceptTouchEvent(arg0);
        else
            return false;
    }

    //设置是否允许滑动，true是可以滑动，false是禁止滑动
    public void setIsCanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }



}
