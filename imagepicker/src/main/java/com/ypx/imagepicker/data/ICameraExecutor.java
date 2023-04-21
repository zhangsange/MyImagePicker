package com.ypx.imagepicker.data;

import androidx.annotation.Nullable;

import com.ypx.imagepicker.bean.ImageItem;

public interface ICameraExecutor {

    void takePhoto(boolean isCopyInDCIM);

    void takeVideo();

    void onTakePhotoResult(@Nullable ImageItem imageItem);
}
