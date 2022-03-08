package com.packy1990.imagepicker;

import static com.ypx.imagepicker.bean.SelectMode.MODE_MULTI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.ypx.imagepicker.editLibrary.utils.FileUtil;
import com.ypx.imagepicker.helper.ToastHelper;
import com.ypx.imagepicker.helper.launcher.PLauncher;

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
    private Button btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);
        mGridLayout = findViewById(R.id.gridLayout);
        checkEdit = findViewById(R.id.isEdit);
        btn_delete = findViewById(R.id.btn_delete);
        checkEdit.setVisibility(View.GONE);
        checkEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCanEdit = isChecked;
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Boolean result =   com.packy1990.imagepicker.FileUtil.deleteAllInDir(FileUtil.storagePath);
                Log.i("哈哈哈",FileUtil.parentPath.getAbsolutePath() + File.separator+"imagePicker_Edit");
              Log.i("哈哈哈", String.valueOf(result));
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
                    // takePhotoToEdit();
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
     * 拍照之后去编辑
     */
    public void takePhotoToEdit() {
        MultiSelectConfig selectConfig = new MultiSelectConfig();
        selectConfig.setDeleteOriginalPic(true);
        selectConfig.setDeleteBeforeEditlPic(false);
        selectConfig.setSingleTakePhoto(true);
        selectConfig.setImgMaxNum(maxCount - picList.size());
        selectConfig.setCompress(false);
        selectConfig.setMaxSize(400);
        selectConfig.setCanEditPic(true);
        selectConfig.setWaterMark("编号122921");
        selectConfig.setWaterMarkColor("#80FF0000");
        selectConfig.setWaterMarkTextSize(20);
        Intent intent = new Intent(MainActivity.this, EasyCameraActivity.class);
        intent.putExtra(Config.CONGIG, selectConfig);
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
    }


    /**
     * 图库选择图片
     *
     * @param count
     */
    public void weChatPick(int count) {
        WeChatPresenter weChatPresenter = new WeChatPresenter();
        ImagePicker.withMulti(weChatPresenter)
                .setMaxCount(count)
                .setSelectMode(MODE_MULTI)//拍照模式  图库拍照按钮拍照完返回图库还是直接进入编辑页面
                .setColumnCount(4)
                .setOriginal(false)
                .setCanEditPic(isCanEdit)//是否脱敏
                .setWaterMark("编号1245789")
                .setWaterMarkTextSize(20f)
                .setWaterMarkColor("#80FF0000")
                .setDeleteOriginalPic(true)
                .setDeleteBeforeEditlPic(false)
                .setImageSavePath(FileUtil.parentPath.getAbsolutePath() + File.separator+"imagePicker_Edit")
                .setOriginal(false)
                .mimeTypes(MimeType.ofImage())
                .filterMimeTypes(MimeType.GIF)
                .setToastHelper(new ToastHelper(){
                    @Override
                    public void showToast(String msg) {
                        Toast.makeText(MainActivity.this,"test",Toast.LENGTH_SHORT).show();
                    }
                })
                .setCompress(false)
                .setMaxSize(400)
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
