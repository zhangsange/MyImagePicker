package com.ypx.imagepicker.helper;

import android.app.Activity;

import com.ypx.imagepicker.bean.ImageItem;

import java.io.Serializable;

/**
 * 时间：2024/3/26 19:30  <br/>
 * 作者：zhangzhi  <br/>
 * 邮箱：crazyhelloworld@163.com  <br/>
 * 描述：
 */
public interface EditDialogHelper extends Serializable {
    void onSaveStart(Activity activity);
    void onSaveProgress(Activity activity, ImageItem savedItem, int curIndex, int total);

    void onSaveFinished(Activity activity);

    /**
     * 返回true，库直接finish editActivity*/
    boolean onBackPressed(Activity activity);
}
