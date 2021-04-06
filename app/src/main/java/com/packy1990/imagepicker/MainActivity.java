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
import com.ypx.imagepicker.bean.selectconfig.MultiSelectConfig;
import com.ypx.imagepicker.cameralibrary.EasyCameraActivity;
import com.ypx.imagepicker.config.Config;
import com.ypx.imagepicker.data.OnImagePickCompleteListener;
import com.ypx.imagepicker.data.OnImagePickCompleteListener2;
import com.ypx.imagepicker.data.PickerActivityCallBack;
import com.ypx.imagepicker.editLibrary.MyIMGEditActivity;
import com.ypx.imagepicker.editLibrary.utils.FileUtil;
import com.ypx.imagepicker.helper.launcher.PLauncher;
import com.ypx.imagepicker.presenter.IPickerPresenter;


import java.io.File;
import java.util.ArrayList;

import static com.ypx.imagepicker.bean.SelectMode.MODE_SINGLE;

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
    private boolean isCanEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);
        mGridLayout = findViewById(R.id.gridLayout);
        checkEdit = findViewById(R.id.isEdit);
        checkEdit.setVisibility(View.GONE);
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
                    MultiSelectConfig selectConfig = new MultiSelectConfig();
                    selectConfig.setDeleteOriginalPic(true);
                    selectConfig.setDeleteBeforeEditlPic(false);
                    selectConfig.setSingleTakePhoto(true);
                    selectConfig.setImgMaxNum(50);
                    selectConfig.setCompress(false);
                    selectConfig.setMaxSize(1000);
                    selectConfig.setCanEditPic(true);
                    selectConfig.setWaterMark("编号122921");
                    selectConfig.setWaterMarkColor("#80FF0000");
                    Intent intent = new Intent(MainActivity.this, EasyCameraActivity.class);
                    intent.putExtra(Config.CONGIG, selectConfig);
                   // intent.putExtra(ImagePicker.INTENT_KEY_PICKER_RESULT, items);
                    PLauncher.init(MainActivity.this).startActivityForResult(intent, PickerActivityCallBack.create(new OnImagePickCompleteListener() {
                        @Override
                        public void onImagePickComplete(ArrayList<ImageItem> items) {
                            picList.addAll(items);
                            for (ImageItem imageItem : picList) {
                                Log.i("看看看 ", "哈哈哈哈 ==  " + imageItem.path);
                            }
                            refreshGridLayout();
                        }
                    }));


//                    intent.putExtra(Config.CONGIG_SHOW_NUMBER, ((MultiSelectConfig)selectConfig).getNumber());
//                    intent.putExtra(Config.CONGIG_NUMBER_COLOR, ((MultiSelectConfig)selectConfig).getNumberColor());
//                    intent.putExtra(Config.CONGIG, selectConfig);
                    // intent.putExtra(ImagePicker.INTENT_KEY_PICKER_RESULT, selectedList);
                    //  activity.startActivity(intent);

//                    openTakePhoto(MainActivity.this, new OnImagePickCompleteListener() {
//                        @Override
//                        public void onImagePickComplete(ArrayList<ImageItem> items) {
//                            picList.addAll(items);
//                            for (ImageItem imageItem : picList) {
//                                Log.i("看看看 ", "哈哈哈哈 ==  " + imageItem.path);
//                            }
//                            refreshGridLayout();
//                        }
//                    });
                    // weChatPick(maxCount - picList.size());
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
                FileUtil.deletePic(MainActivity.this, picList.get(pos).path);
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
        ImagePicker.preview(this, weChatPresenter, picList, pos, isCanEdit, new OnImagePickCompleteListener() {
            @Override
            public void onImagePickComplete(ArrayList<ImageItem> items) {
                //图片选择回调，主线程
                picList.clear();
                picList.addAll(items);
                refreshGridLayout();
            }


        });
    }

    /**
     * @param context 开启编辑预览
     */
    public static void openTakePhoto(final Activity context, final OnImagePickCompleteListener listener) {
        String imageName = System.currentTimeMillis() + "";
        boolean isCopyInDCIM = true;
        ImagePicker.takePhoto(context, imageName, isCopyInDCIM, new OnImagePickCompleteListener() {
            @Override
            public void onImagePickComplete(ArrayList<ImageItem> items) {
                if (items != null && items.size() > 0) {
                    MultiSelectConfig selectConfig = new MultiSelectConfig();
                    selectConfig.setDeleteOriginalPic(true);
                    selectConfig.setDeleteBeforeEditlPic(false);
                    selectConfig.setSingleTakePhoto(true);
                    if (true) {//
                        Intent intent = new Intent(context, MyIMGEditActivity.class);
                        intent.putExtra(Config.CONGIG_SHOW_NUMBER, "ipInfo.waterMark");
                        intent.putExtra(Config.CONGIG_NUMBER_COLOR, "#80FF0000");
                        intent.putExtra(Config.CONGIG, selectConfig);
                        intent.putExtra(ImagePicker.INTENT_KEY_PICKER_RESULT, items);
                        //    context.startActivity(intent);
                        PLauncher.init(context).startActivityForResult(intent, PickerActivityCallBack.create(listener));
                    }
                }
            }
        });
    }

    public void weChatPick(int count) {
        WeChatPresenter weChatPresenter = new WeChatPresenter();
        ImagePicker.withMulti(weChatPresenter)
                .setMaxCount(maxCount)
                .setSelectMode(MODE_SINGLE)
                .setColumnCount(4)
                .setOriginal(false)
                .setCanEditPic(isCanEdit)//是否脱敏
                .setNumber("编号1245")
                .setNumberColor("#80FF0000")
                .setDeleteOriginalPic(false)
                .setDeleteBeforeEditlPic(false)
                .setOriginal(false)
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
                        for (ImageItem imageItem : picList) {
                            Log.i("看看看 ", "哈哈哈哈 ==  " + imageItem.path);
                        }
                        refreshGridLayout();
                    }

                });


    }

}
