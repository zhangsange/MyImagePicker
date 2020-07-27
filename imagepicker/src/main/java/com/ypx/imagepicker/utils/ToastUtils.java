package com.ypx.imagepicker.utils;

import android.content.Context;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;


/**
 * 时间：2019/11/11 12:22  <br/>
 * 作者：zhangzhi  <br/>
 * 邮箱：crazyhelloworld@163.com  <br/>
 * 描述：
 */
public class ToastUtils {
    static {
        Toasty.Config.getInstance().allowQueue(false).apply();
    }

    public static void showToastError(Context context, String msg) {
//       Toast toasty = Toasty.error(ZBaseApplication.instance, msg, Toast.LENGTH_SHORT, true);
//       toasty.setGravity(Gravity.CENTER,0,0);
//       toasty.show();
        Toasty.error(context, msg, Toast.LENGTH_SHORT, true).show();
    }

    public static void showToastSuccess(Context context,String msg) {
        Toasty.success(context, msg, Toast.LENGTH_SHORT, true).show();
    }

    public static void showToastInfo(Context context,String msg) {
        Toasty.info(context, msg, Toast.LENGTH_SHORT, true).show();
    }

    public static void showToastWarning(Context context,String msg) {
        Toasty.warning(context, msg, Toast.LENGTH_SHORT, true).show();
    }

    public static void showToastNormal(Context context,String msg) {
        Toasty.normal(context, msg).show();
    }

}
