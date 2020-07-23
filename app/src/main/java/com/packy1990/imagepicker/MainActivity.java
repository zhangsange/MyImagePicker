package com.packy1990.imagepicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.packy1990.imagepicker.style.RedBookPresenter;
import com.packy1990.imagepicker.style.WeChatPresenter;
import com.packy1990.imagepicker.style.custom.CustomImgPickerPresenter;
import com.ypx.imagepicker.ImagePicker;
import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.bean.MimeType;
import com.ypx.imagepicker.bean.PickerError;
import com.ypx.imagepicker.data.OnImagePickCompleteListener;
import com.ypx.imagepicker.data.OnImagePickCompleteListener2;
import com.ypx.imagepicker.editLibrary.utils.FileUtil;
import com.ypx.imagepicker.presenter.IPickerPresenter;


import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MainActivityView mainActivityView;
    private WeChatPresenter weChatPresenter;
    private RedBookPresenter redBookPresenter;
    private CustomImgPickerPresenter customImgPickerPresenter;
    final int maxCount = 30;
    private ArrayList<ImageItem> picList = new ArrayList<>();
    public static boolean isAutoJumpAlohaActivity;
    private GridLayout mGridLayout;
    private CheckBox checkEdit;
    private boolean isCanEdit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);
        mGridLayout = findViewById(R.id.gridLayout);


        checkEdit = findViewById(R.id.isEdit);
        checkEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCanEdit = isChecked;
            }
        });


        weChatPresenter = new WeChatPresenter();
        redBookPresenter = new RedBookPresenter();
        customImgPickerPresenter = new CustomImgPickerPresenter();
        picList = new ArrayList<>();
        refreshGridLayout();
        //mainActivityView = MainActivityView.create(this, this);
    }

    /**
     * 刷新图片显示
     */
    private void refreshGridLayout() {
        mGridLayout.setVisibility(View.VISIBLE);
        mGridLayout.removeAllViews();
        int num = picList.size();
        final int picSize = (getScreenWidth() - dp(20)) / 4;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(picSize, picSize);
        if (num >= maxCount) {
            mGridLayout.setVisibility(View.VISIBLE);
            for (int i = 0; i < num; i++) {
                RelativeLayout view = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.a_layout_pic_select, null);
                view.setLayoutParams(params);
                view.setPadding(dp(5), dp(5), dp(5), dp(5));
                setPicItemClick(view, i);
                mGridLayout.addView(view);
            }
        } else {
            mGridLayout.setVisibility(View.VISIBLE);
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(params);
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.add_pic));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(dp(5), dp(5), dp(5), dp(5));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    weChatPick(maxCount - picList.size());
                }
            });
            for (int i = 0; i < num; i++) {
                RelativeLayout view = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.a_layout_pic_select, null);
                view.setLayoutParams(params);
                view.setPadding(dp(5), dp(5), dp(5), dp(5));
                setPicItemClick(view, i);
                mGridLayout.addView(view);
            }
            mGridLayout.addView(imageView);
        }
    }

    private void setPicItemClick(RelativeLayout layout, final int pos) {
        ImageView iv_pic = (ImageView) layout.getChildAt(0);
        ImageView iv_close = (ImageView) layout.getChildAt(1);
        displayImage(picList.get(pos), iv_pic);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // if (picList.get(pos).path.contains("photo_edit")) {
                    deletePic(picList.get(pos).path);
                FileUtil.deletePic(MainActivity.this,picList.get(pos).path);
                //updateFileFromDatabase(MainActivity.this,picList.get(pos).path);
               // }
                picList.remove(pos);
                refreshGridLayout();
            }
        });
        iv_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                preview(pos);
            }
        });
    }

    private void deletePic(String path) {
        File file = new File(path);
        //删除系统缩略图
        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{path});
        //删除手机中图片
        file.delete();
    }

    private void displayImage(ImageItem imageItem, ImageView imageView) {
        if (imageItem.getCropUrl() != null && imageItem.getCropUrl().length() > 0) {
            Glide.with(this).load(imageItem.getCropUrl()).into(imageView);
        } else {
            if (imageItem.getUri() != null) {
                Glide.with(this).load(imageItem.getUri()).into(imageView);
            } else {
                Glide.with(this).load(imageItem.path).into(imageView);
            }
        }
    }


    private int dp(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    /**
     * 获得屏幕宽度
     */
    private int getScreenWidth() {
        WindowManager wm = (WindowManager) getSystemService(Activity.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }


    public void preview(int pos) {
        final ArrayList<ImageItem> resultList = picList;
        IPickerPresenter presenter = weChatPresenter;

        //这一段是为了解决预览加载的是原图而不是剪裁的图片，做的兼融处理，实际调用请删除这一段
        ArrayList<String> list = new ArrayList<>();
        for (ImageItem imageItem : resultList) {
            if (imageItem.getCropUrl() != null && imageItem.getCropUrl().length() > 0) {
                list.add(imageItem.getCropUrl());
            } else {
                list.add(imageItem.path);
            }
        }

        ImagePicker.preview(this, presenter, list, pos, isCanEdit,true,new OnImagePickCompleteListener() {
            @Override
            public void onImagePickComplete(ArrayList<ImageItem> items) {
                //图片编辑回调，主线程
//                for (int i = 0; i < items.size(); i++) {
//                    Log.i("ddd", "粗来哈哈哈哈222 = " + items.get(i).path);
//                }
                resultList.clear();
                //图片选择回调，主线程
                picList.clear();
                picList.addAll(items);
                refreshGridLayout();
               // displayImage(picList.get(0), imageView);
            }


        });
    }


    public void weChatPick(int count) {
        WeChatPresenter weChatPresenter = new WeChatPresenter();
        ImagePicker.withMulti(weChatPresenter)
                .setMaxCount(maxCount)
                .setColumnCount(4)
                .setOriginal(true)
                .setCanEditPic(isCanEdit)//是否脱敏
                .setNumber("编号1245")
                .mimeTypes(MimeType.ofImage())
                .filterMimeTypes(MimeType.GIF)
                .showCamera(true)
                .setPreview(true)
                .setLastImageList(picList)
                .pick(this, new OnImagePickCompleteListener2() {
                    @Override
                    public void onPickFailed(PickerError error) {
//                        if (error.getCode()!= PickerError.CANCEL.getCode()) {
//                            ToastUtils.showToastError(error.getMessage());
//                        }
                    }

                    @Override
                    public void onImagePickComplete(ArrayList<ImageItem> items) {
                        picList.clear();
                        picList.addAll(items);
                        refreshGridLayout();
                    }

                });


//        boolean isCustom = mainActivityView.isCustom();
//        boolean isShowOriginal = mainActivityView.isShowOriginal();
//        Set<MimeType> mimeTypes = mainActivityView.getMimeTypes();
//        int selectMode = mainActivityView.getSelectMode();
//        boolean isCanPreviewVideo = mainActivityView.isCanPreviewVideo();
//        boolean isShowCamera = mainActivityView.isShowCamera();
//        boolean isPreviewEnable = mainActivityView.isPreviewEnable();
//        boolean isVideoSinglePick = mainActivityView.isVideoSinglePick();
//        boolean isSinglePickWithAutoComplete = mainActivityView.isSinglePickWithAutoComplete();
//        boolean isSinglePickImageOrVideoType = mainActivityView.isSinglePickImageOrVideoType();
//        ArrayList<ImageItem> resultList = mainActivityView.getPicList();
//        boolean isCheckLastImageList = mainActivityView.isCheckLastImageList();
//        boolean isCheckShieldList = mainActivityView.isCheckShieldList();
//
//        IPickerPresenter presenter = isCustom ? customImgPickerPresenter : weChatPresenter;
//        ImagePicker.withMulti(presenter)//指定presenter
//                .setMaxCount(count)//设置选择的最大数
//                .setColumnCount(4)//设置列数
//                .setOriginal(isShowOriginal)
//                .mimeTypes(mimeTypes)//设置要加载的文件类型，可指定单一类型
//                // .filterMimeType(MimeType.GIF)//设置需要过滤掉加载的文件类型
//                .setSelectMode(selectMode)
//                .setDefaultOriginal(false)
//                .setCanEditPic(true)
//                .setNumber("编码1234")
//                .setPreviewVideo(isCanPreviewVideo)
//                .showCamera(isShowCamera)//显示拍照
//                .showCameraOnlyInAllMediaSet(true)
//                .setPreview(isPreviewEnable)//是否开启预览
//                .setVideoSinglePick(isVideoSinglePick)//设置视频单选
//                .setSinglePickWithAutoComplete(isSinglePickWithAutoComplete)
//                .setSinglePickImageOrVideoType(isSinglePickImageOrVideoType)//设置图片和视频单一类型选择
//                .setMaxVideoDuration(120000L)//设置视频可选取的最大时长
//                .setMinVideoDuration(5000L)
//                .setSingleCropCutNeedTop(true)
//                //设置上一次操作的图片列表，下次选择时默认恢复上一次选择的状态
//                .setLastImageList(isCheckLastImageList ? resultList : null)
//                //设置需要屏蔽掉的图片列表，下次选择时已屏蔽的文件不可选择
//                .setShieldList(isCheckShieldList ? resultList : null)
//                .pick(this, new OnImagePickCompleteListener2() {
//                    @Override
//                    public void onPickFailed(PickerError error) {
//                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onImagePickComplete(ArrayList<ImageItem> items) {
//                        //图片选择回调，主线程
//                        mainActivityView.notifyImageItemsCallBack(items);
//                    }
//                });

    }

//
//    public void redBookPick(int count) {
//        boolean isShowCamera = mainActivityView.isShowCamera();
//        Set<MimeType> mimeTypes = mainActivityView.getMimeTypes();
//        ArrayList<ImageItem> resultList = mainActivityView.getPicList();
//        boolean isVideoSinglePick = mainActivityView.isVideoSinglePick();
//        boolean isSinglePickWithAutoComplete = mainActivityView.isSinglePickWithAutoComplete();
//
//        ImagePicker.withCrop(redBookPresenter)//设置presenter
//                .setMaxCount(count)//设置选择数量
//                .showCamera(isShowCamera)//设置显示拍照
//                .setColumnCount(4)//设置列数
//                .mimeTypes(mimeTypes)//设置需要加载的文件类型
//                // .filterMimeType(MimeType.GIF)//设置需要过滤掉的文件类型
//                .assignGapState(false)
//                .setFirstImageItem(resultList.size() > 0 ? resultList.get(0) : null)//设置上一次选中的图片
//                .setVideoSinglePick(isVideoSinglePick)//设置视频单选
//                .setSinglePickWithAutoComplete(isSinglePickWithAutoComplete)
//                .setMaxVideoDuration(120000L)//设置可选区的最大视频时长
//                .setMinVideoDuration(5000L)
//                .pick(this, new OnImagePickCompleteListener2() {
//                    @Override
//                    public void onPickFailed(PickerError error) {
//                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onImagePickComplete(ArrayList<ImageItem> items) {
//                        //图片剪裁回调，主线 程
//                        //注意：剪裁回调里的ImageItem中getCropUrl()才是剪裁过后的图片地址
//                        mainActivityView.notifyImageItemsCallBack(items);
//                    }
//                });
//    }

//    @Override
//    public void pickAndCrop() {
//        boolean isCustom = mainActivityView.isCustom();
//        Set<MimeType> mimeTypes = mainActivityView.getMimeTypes();
//        boolean isShowCamera = mainActivityView.isShowCamera();
//        int minMarginProgress = mainActivityView.getMinMarginProgress();
//        boolean isGap = mainActivityView.isGap();
//        int cropGapBackgroundColor = mainActivityView.getCropGapBackgroundColor();
//        int cropRatioX = mainActivityView.getCropRatioX();
//        int cropRatioY = mainActivityView.getCropRatioY();
//        boolean isNeedCircle = mainActivityView.isNeedCircle();
//
//        IPickerPresenter presenter = isCustom ? customImgPickerPresenter : weChatPresenter;
//        MultiPickerBuilder builder = ImagePicker.withMulti(presenter)//指定presenter
//                .setColumnCount(4)//设置列数
//                .mimeTypes(mimeTypes)//设置要加载的文件类型，可指定单一类型
//                // .filterMimeType(MimeType.GIF)//设置需要过滤掉加载的文件类型
//                .setSingleCropCutNeedTop(true)
//                .showCamera(isShowCamera)//显示拍照
//                .cropSaveInDCIM(false)
//                .cropRectMinMargin(minMarginProgress)
//                .cropStyle(isGap ? CropConfig.STYLE_GAP : CropConfig.STYLE_FILL)
//                .cropGapBackgroundColor(cropGapBackgroundColor)
//                .setCropRatio(cropRatioX, cropRatioY);
//        if (isNeedCircle) {
//            builder.cropAsCircle();
//        }
//        builder.crop(this, new OnImagePickCompleteListener() {
//            @Override
//            public void onImagePickComplete(ArrayList<ImageItem> items) {
//                //图片选择回调，主线程
//                mainActivityView.notifyImageItemsCallBack(items);
//            }
//        });
//    }

//    @Override
//    public void autoCrop() {
//        ArrayList<ImageItem> resultList = mainActivityView.getPicList();
//        int minMarginProgress = mainActivityView.getMinMarginProgress();
//        boolean isGap = mainActivityView.isGap();
//        int cropGapBackgroundColor = mainActivityView.getCropGapBackgroundColor();
//        int cropRatioX = mainActivityView.getCropRatioX();
//        int cropRatioY = mainActivityView.getCropRatioY();
//        boolean isNeedCircle = mainActivityView.isNeedCircle();
//
//        if (resultList.size() == 0) {
//            Toast.makeText(this, "请至少选择一张图片", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        //配置剪裁属性
//        CropConfig cropConfig = new CropConfig();
//        cropConfig.setCropRatio(cropRatioX, cropRatioY);//设置剪裁比例
//        cropConfig.setCropRectMargin(minMarginProgress);//设置剪裁框间距，单位px
//        cropConfig.setCircle(isNeedCircle);//是否圆形剪裁
//        cropConfig.setCropStyle(isGap ? CropConfig.STYLE_GAP : CropConfig.STYLE_FILL);
//        cropConfig.setCropGapBackgroundColor(cropGapBackgroundColor);
//        //用于恢复上一次剪裁状态
//        //cropConfig.setCropRestoreInfo();
//        ImagePicker.crop(this, new WeChatPresenter(), cropConfig, resultList.get(0).path, new OnImagePickCompleteListener2() {
//            @Override
//            public void onPickFailed(PickerError error) {
//                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onImagePickComplete(ArrayList<ImageItem> items) {
//                //剪裁回调，主线程
//                mainActivityView.notifyImageItemsCallBack(items);
//            }
//        });
//    }

//    @Override
//    public void takePhoto() {
//        String imageName = System.currentTimeMillis() + "";
//        boolean isCopyInDCIM = true;
//        ImagePicker.takePhoto(this, imageName, isCopyInDCIM, new OnImagePickCompleteListener() {
//            @Override
//            public void onImagePickComplete(ArrayList<ImageItem> items) {
//                mainActivityView.notifyImageItemsCallBack(items);
//            }
//        });
//    }

//    @Override
//    public void takePhotoAndCrop() {
//        int minMarginProgress = mainActivityView.getMinMarginProgress();
//        boolean isGap = mainActivityView.isGap();
//        int cropGapBackgroundColor = mainActivityView.getCropGapBackgroundColor();
//        int cropRatioX = mainActivityView.getCropRatioX();
//        int cropRatioY = mainActivityView.getCropRatioY();
//        boolean isNeedCircle = mainActivityView.isNeedCircle();
//        boolean isCustom = mainActivityView.isCustom();
//
//        //配置剪裁属性
//        CropConfig cropConfig = new CropConfig();
//        cropConfig.setCropRatio(cropRatioX, cropRatioY);//设置剪裁比例
//        cropConfig.setCropRectMargin(minMarginProgress);//设置剪裁框间距，单位px
//        cropConfig.setCircle(isNeedCircle);//是否圆形剪裁
//        cropConfig.setCropStyle(isGap ? CropConfig.STYLE_GAP : CropConfig.STYLE_FILL);
//        cropConfig.setCropGapBackgroundColor(cropGapBackgroundColor);
//
//        IPickerPresenter presenter = isCustom ? customImgPickerPresenter : weChatPresenter;
//        ImagePicker.takePhotoAndCrop(this, presenter, cropConfig, new OnImagePickCompleteListener() {
//            @Override
//            public void onImagePickComplete(ArrayList<ImageItem> items) {
//                //剪裁回调，主线程
//
//                mainActivityView.notifyImageItemsCallBack(items);
//            }
//        });
//    }


}
