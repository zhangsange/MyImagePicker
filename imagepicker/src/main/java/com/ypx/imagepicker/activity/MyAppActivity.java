package com.ypx.imagepicker.activity;

import android.graphics.Bitmap;

import java.util.List;

/**
 * time：2021-04-06
 * author：pachy1990
 * 描述：
 */
public class MyAppActivity   {

    private static List<Bitmap> bitmapList;

    public static List<Bitmap> getBitmapList() {
        return bitmapList;
    }

    public static void setBitmapList(List<Bitmap> bitmapList) {
        MyAppActivity.bitmapList = bitmapList;
    }




}
