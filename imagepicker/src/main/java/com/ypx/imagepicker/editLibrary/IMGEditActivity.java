package com.ypx.imagepicker.editLibrary;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.ypx.imagepicker.ImagePicker;
import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.config.Config;
import com.ypx.imagepicker.editLibrary.core.IMGMode;
import com.ypx.imagepicker.editLibrary.core.IMGText;
import com.ypx.imagepicker.editLibrary.utils.FileUtil;
import com.ypx.imagepicker.editLibrary.utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 2017/11/14 下午2:26.
 */

public class IMGEditActivity extends IMGEditBaseActivity {

    private static final int MAX_WIDTH = 1024;

    private static final int MAX_HEIGHT = 1024;

    public static final String EXTRA_IMAGE_URI = "IMAGE_URI";

    public static final String EXTRA_IMAGE_SAVE_PATH = "IMAGE_SAVE_PATH";
    private String path;
    private String url;
    private String deleteUrl;
    private String number;



    @Override
    public String getNumber() {
        return number;
    }

    @Override
    public ArrayList<ImageItem> imageItemList() {
        imageItemList = (ArrayList<ImageItem>) getIntent().getSerializableExtra(ImagePicker.INTENT_KEY_PICKER_RESULT);
        return  imageItemList;
    }

    @Override
    public List<Bitmap> getBitmapList() {
        number = getIntent().getStringExtra(Config.CONGIG_SHOW_NUMBER);
        if (imageItemList != null && imageItemList.size() > 0) {
            for (int i = 0; i < imageItemList.size(); i++) {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageItemList.get(i).getUri());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bitmapList.add(bitmap);
            }
            return bitmapList;

        } else {
            return null;
        }


    }



    @Override
    public void onText(IMGText text) {
        mImgView.addStickerText(text, false);
    }

    @Override
    public void onModeClick(IMGMode mode) {
        IMGMode cm = mImgView.getMode();
        if (cm == mode) {
            mode = IMGMode.NONE;
        }
        mImgView.setMode(mode);
        updateModeUI();

//        if (mode == IMGMode.CLIP) {
//            setOpDisplay(OP_CLIP);
//        }
    }

    @Override
    public void onUndoClick() {
        IMGMode mode = mImgView.getMode();
        if (mode == IMGMode.SHADE) {
            mImgView.undoShade();
        } else if (mode == IMGMode.DOODLE) {
            mImgView.undoDoodle();
        } else if (mode == IMGMode.MOSAIC) {
            mImgView.undoMosaic();
        }
    }

    @Override
    public void onCancelClick() {
        // deletePic();//删除图片
        finish();
    }

    @Override
    public void onDoneClick() {

        for (int i = 0 ; i<imageItemList.size();i++){
            if (!imageItemList.get(i).path.startsWith("http")){
                FileUtil.deletePic(getApplication(),imageItemList.get(i).path);
            }
//            if (imageItemList.get(i).path.contains("photo_edit")){
//                FileUtil.deletePic(getApplication(),imageItemList.get(i).path);
//            }
        }
        for (int i = 0; i < bitmapList.size(); i++) {
            if (SystemUtils.beforeAndroidTen()) {
                imageItemList.get(i).path = FileUtil.saveBitmap("", bitmapList.get(i),this);
            } else {
                imageItemList.get(i).path = FileUtil.saveBitmapAndroidQ(this, "", bitmapList.get(i));
            }
        }

        imageItemList=  ImagePicker.transitArray(this, imageItemList);

        ImagePicker.closePickerWithCallback(imageItemList);
        finish();
        return;

    }

    private void deletePic(String imgPath) {

        File file = new File(path);
        //删除系统缩略图
        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{path});
        //删除手机中图片
        file.delete();
    }

    @Override
    public void onCancelClipClick() {
        mImgView.cancelClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onDoneClipClick() {
        mImgView.doClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }

    @Override
    public void onResetClipClick() {
        mImgView.resetClip();
    }

    @Override
    public void onRotateClipClick() {
        mImgView.doRotate();
    }

    @Override
    public void onColorChanged(int checkedColor) {
        mImgView.setPenColor(checkedColor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageItemList.clear();
    }
}
