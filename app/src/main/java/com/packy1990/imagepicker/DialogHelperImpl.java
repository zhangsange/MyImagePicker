package com.packy1990.imagepicker;

import android.app.Activity;

import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.helper.EditDialogHelper;

/**
 * 时间：2024/3/26 19:33  <br/>
 * 作者：zhangzhi  <br/>
 * 邮箱：crazyhelloworld@163.com  <br/>
 * 描述：
 */
class DialogHelperImpl implements EditDialogHelper {

   @Override
   public void onSaveStart(Activity activity) {
      System.out.println("=======onSaveStart");
   }

   @Override
   public void onSaveProgress(Activity activity, ImageItem savedItem, int curIndex, int total) {
      System.out.println("======"+curIndex+"/"+total);
   }

   @Override
   public void onSaveFinished(Activity activity) {
      System.out.println("=======保存成功");
   }

   @Override
   public boolean onBackPressed(Activity activity) {
      System.out.println("=======返回");
      return true;
   }


}
