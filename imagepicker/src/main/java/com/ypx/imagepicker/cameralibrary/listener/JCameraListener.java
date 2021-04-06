package com.ypx.imagepicker.cameralibrary.listener;

import android.graphics.Bitmap;

import java.util.List;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/4/26
 * 描    述：
 * =====================================
 */
public interface JCameraListener {

    void captureSuccess(List<Bitmap> bitmapList);

    void recordSuccess(String url, Bitmap firstFrame);
    void back();

}
