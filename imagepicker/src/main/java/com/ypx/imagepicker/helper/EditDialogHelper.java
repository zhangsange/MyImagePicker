package com.ypx.imagepicker.helper;

import android.app.Activity;

import java.io.Serializable;

/**
 * 时间：2024/3/26 19:30  <br/>
 * 作者：zhangzhi  <br/>
 * 邮箱：crazyhelloworld@163.com  <br/>
 * 描述：
 */
public interface EditDialogHelper extends Serializable {
    void onPicSave(Activity activity,int curIndex, int total);

    void saveFinished(Activity activity);

    /**
     * 返回true，库直接finish editActivity*/
    boolean onBackPressed(Activity activity);
}
