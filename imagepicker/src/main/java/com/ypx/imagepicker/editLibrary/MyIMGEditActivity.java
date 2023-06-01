package com.ypx.imagepicker.editLibrary;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.FileUtils;
import com.ypx.imagepicker.ImagePicker;
import com.ypx.imagepicker.R;
import com.ypx.imagepicker.activity.MyAppActivity;
import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.bean.selectconfig.MultiSelectConfig;
import com.ypx.imagepicker.config.Config;
import com.ypx.imagepicker.constant.Code;
import com.ypx.imagepicker.editLibrary.core.IMGMode;
import com.ypx.imagepicker.editLibrary.core.IMGText;
import com.ypx.imagepicker.editLibrary.listener.SelectStatusListener;
import com.ypx.imagepicker.editLibrary.utils.FileUtil;
import com.ypx.imagepicker.editLibrary.utils.SystemUtils;
import com.ypx.imagepicker.editLibrary.view.IMGColorGroup;
import com.ypx.imagepicker.editLibrary.view.IMGColorRadio;
import com.ypx.imagepicker.editLibrary.view.IMGView;

import java.util.ArrayList;
import java.util.List;

import ren.perry.perry.LoadingDialog;

/**
 * time：2021-09-03
 * author：pachy1990
 * 描述：
 */
public class MyIMGEditActivity extends AppCompatActivity implements View.OnClickListener, IMGTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener, DialogInterface.OnShowListener,
        DialogInterface.OnDismissListener {

    protected IMGView mImgView;

    private RadioGroup mModeGroup;

    private IMGColorGroup mColorGroup;

    private IMGTextEditDialog mTextDialog;


    private ViewSwitcher mOpSwitcher, mOpSubSwitcher;

    public ArrayList<ImageItem> imageItemList = new ArrayList<>();
    public ArrayList<ImageItem> imageLocList = new ArrayList<>();
    public ArrayList<String> imageSelectList = new ArrayList<>();
    public ArrayList<ImageItem> imageHttpList = new ArrayList<>();
    public List<Bitmap> bitmapList = new ArrayList<>();
    private TextView tvDone;
    private String waterMark;
    private LoadingDialog dialog;
    private String waterMarkColor;
    private MultiSelectConfig selectConfig;
    private boolean isDeleteOriginalPic = false; //是否删除原图
    private boolean isDeleteBeforeEditlPic = false;//是否删除编辑后的图片
    private boolean isSingleTakePhoto = false;//是否为拍照后直接脱敏
    private float waterMarkTextSize = 12f;
    private MyViewPager mViewPager;
    private List<IMGView> imgViewList;
    private SelectStatusListener selectStatusListener;
    private TextView tvPage;
    private View mLayoutOpSub;
    public static final int OP_SUB_DOODLE = 0;

    public static final int OP_SUB_MOSAIC = 1;
    public static final int OP_SUB_SHADE = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_edit_vp_activity);
        selectConfig = (MultiSelectConfig) getIntent().getSerializableExtra(Config.CONGIG);
        if (selectConfig != null) {
            isSingleTakePhoto = selectConfig.isSingleTakePhoto();
            waterMark = selectConfig.getWaterMark();
            waterMarkTextSize = selectConfig.getWaterMarkTextSize();
            waterMarkColor = selectConfig.getWaterMarkColor();
            isDeleteOriginalPic = selectConfig.isDeleteOriginalPic();
            isDeleteBeforeEditlPic = selectConfig.isDeleteBeforeEditlPic();
        }
        initViews();
        if (!isSingleTakePhoto) {//图库进入编辑页面
            imageItemList = (ArrayList<ImageItem>) getIntent().getSerializableExtra(ImagePicker.INTENT_KEY_PICKER_RESULT);
            initImage();
            showLoading("正在加载图片中,请稍等...");
            new Thread(new Runnable() {
                Message msg = Message.obtain();

                @Override
                public void run() {
                    bitmapList = getBitmapList();
                    msg.what = 0x101;
                    if (mHandler != null) {
                        mHandler.sendMessage(msg);
                    }
                }
            }).start();
        } else {//拍照进入编辑页面
            imageItemList = new ArrayList<>();
            bitmapList = MyAppActivity.getBitmapList();
            initImageBitmap();
            initData();
        }

    }

    public void setOpSubDisplay(int opSub) {
        if (opSub < 0) {
            mLayoutOpSub.setVisibility(View.GONE);
        } else {
            if (selectConfig.isShowColorChoose()) {
                mOpSubSwitcher.setDisplayedChild(opSub);
                mLayoutOpSub.setVisibility(View.VISIBLE);
            }
        }
    }

    public int dp(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5);
    }

    private void resetShadowColors() {
        mColorGroup.removeAllViews();
        int dp28 = dp(28);
        int dp6 = dp(6);
        for (String shadowColor : selectConfig.shadowColors) {
            IMGColorRadio radio = new IMGColorRadio(this);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(dp28, dp28);
            params.setMargins(dp6, dp6, dp6, dp6);
            radio.setId(View.generateViewId());
            radio.setLayoutParams(params);
            radio.setClickable(true);
            radio.setColor(Color.parseColor(shadowColor));
            mColorGroup.addView(radio);
        }
    }

    private void resetColorPenChecked() {
        ((IMGColorRadio) mColorGroup.getChildAt(0)).setChecked(true);
        for (int i = 0; i < mColorGroup.getChildCount(); i++) {
            IMGColorRadio colorRadio = ((IMGColorRadio) mColorGroup.getChildAt(i));
            if (colorRadio.getColor() == ImagePicker.getEditPicPenColor()) {
                colorRadio.setChecked(true);
                break;
            }
        }
    }

    private void initViews() {
        mViewPager = findViewById(R.id.img_vp);
        tvDone = findViewById(R.id.tv_done);
        mModeGroup = findViewById(R.id.rg_modes);
        mOpSwitcher = findViewById(R.id.vs_op);
        mOpSubSwitcher = findViewById(R.id.vs_op_sub);
        mColorGroup = findViewById(R.id.cg_colors);
        mColorGroup.setOnCheckedChangeListener(this);
        if (selectConfig.shadowColors != null && selectConfig.shadowColors.size() > 0) {
            resetShadowColors();
        }
        resetColorPenChecked();

        tvPage = findViewById(R.id.tv_page);
        mLayoutOpSub = findViewById(R.id.layout_op_sub);

        RadioButton rbShade = findViewById(R.id.rb_shade);
        if (selectConfig.isShowShadeBtn()) {
            rbShade.setVisibility(View.VISIBLE);
            if (!selectConfig.getShadeBtnText().isEmpty()) rbShade.setText(selectConfig.getShadeBtnText());
            if (selectConfig.getShadeBtnDrawable() != null) rbShade.setButtonDrawable(selectConfig.getShadeBtnDrawable());
        } else rbShade.setVisibility(View.GONE);

        RadioButton rbDoodle = findViewById(R.id.rb_doodle);
        if (selectConfig.isShowDoodleBtn()) {
            rbDoodle.setVisibility(View.VISIBLE);
            if (!selectConfig.getDoodleBtnText().isEmpty()) rbDoodle.setText(selectConfig.getDoodleBtnText());
            if (selectConfig.getDoodleBtnDrawable() != null) rbDoodle.setButtonDrawable(selectConfig.getDoodleBtnDrawable());
        } else rbDoodle.setVisibility(View.GONE);

        RadioButton rbMosaic = findViewById(R.id.rb_mosaic);
        if (selectConfig.isShowMosaicBtn()) {
            rbMosaic.setVisibility(View.VISIBLE);
            if (!selectConfig.getMosaicBtnText().isEmpty()) rbMosaic.setText(selectConfig.getMosaicBtnText());
            if (selectConfig.getMosaicBtnDrawable() != null) rbMosaic.setButtonDrawable(selectConfig.getMosaicBtnDrawable());
        } else rbMosaic.setVisibility(View.GONE);

        RadioButton rbText = findViewById(R.id.btn_text);
        if (selectConfig.isShowTextBtn()) {
            rbText.setVisibility(View.VISIBLE);
            if (!selectConfig.getTextBtnText().isEmpty()) rbText.setText(selectConfig.getTextBtnText());
            if (selectConfig.getTextBtnDrawable() != null) rbText.setButtonDrawable(selectConfig.getTextBtnDrawable());
        } else rbText.setVisibility(View.GONE);

//        mPreviewRecyclerView = findViewById(R.id.mPreviewRecyclerView);
//        initPreviewList();

    }

    /**
     *
     */
    private void initImageBitmap() {
        for (Bitmap imageItem : bitmapList) {
            imageLocList.add(new ImageItem(""));
        }
    }

    /**
     * 处理排除网络图片
     */
    private void initImage() {
        if (null != imageItemList) {
            for (ImageItem imageItem : imageItemList) {
                if (imageItem.path.startsWith("http")) {
                    imageHttpList.add(imageItem);
                } else {
                    imageLocList.add(imageItem);
                }
            }
        }
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            hideLoading();
            if (msg.what == 0x101) {
                initData();
            } else if (msg.what == 0x102) {
                if (isSingleTakePhoto) {
                    Intent intent = new Intent();
                    intent.putExtra(ImagePicker.INTENT_KEY_PICKER_RESULT, imageItemList);
                    setResult(Code.REQUEST_EDIT, intent);
                    finish();
                } else {
                    ImagePicker.closePickerWithCallback(imageItemList);
                }
                finish();
            }

            return false;
        }
    });

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    class MyAdapter extends PagerAdapter {
        List<IMGView> imgViews = new ArrayList<>();

        public MyAdapter(List<IMGView> imgViewList) {
            imgViews = imgViewList;
        }

        @Override
        public int getCount() {
            return imgViews.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {
            IMGView imgView = imgViews.get(position);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT);
            imgView.setLayoutParams(params);
            imgView.setBackgroundColor(Color.BLACK);
            imgView.setImageBitmap(bitmapList.get(position));
            if (!TextUtils.isEmpty(waterMark) && !imageLocList.get(position).path.contains(FileUtil.PIC_EDIT_FOLDER_NAME)) {
                if (!TextUtils.isEmpty(waterMarkColor)) {
                    imgView.addStickerText(new IMGText(waterMark, Color.parseColor(waterMarkColor), waterMarkTextSize), true);
                } else {
                    imgView.addStickerText(new IMGText(waterMark, Color.RED, waterMarkTextSize), true);
                }
            }
            container.addView(imgView);
            return imgView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);

        }

        //解决ViewPager不刷新的问题
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


    }

    private void initData() {
        selectStatusListener = new SelectStatusListener() {
            @Override
            public void onSelectedStatus(Boolean status) {
                mModeGroup.check(R.id.rb_shade);
                setViewPagerIsCanScroll(false);
            }

            @Override
            public void onChangeMode() {
                mImgView.setMode(IMGMode.SHADE);
            }
        };
        if (bitmapList != null && bitmapList.size() > 0) {
            tvPage.setText(1 + " / " + bitmapList.size());
            imgViewList = new ArrayList<>();
            for (Bitmap bitmap : bitmapList) {
                IMGView imgView = new IMGView(this);
                imgView.setPenColor(ImagePicker.getEditPicPenColor());
                imgView.setSelectStatusListener(selectStatusListener);
                imgViewList.add(imgView);
            }
            mImgView = imgViewList.get(0);
            MyAdapter vpAdapter = new MyAdapter(imgViewList);
            mViewPager.setAdapter(vpAdapter);
            mViewPager.setOffscreenPageLimit(51);
            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    mImgView = imgViewList.get(position);
                    tvPage.setText(position + 1 + " / " + bitmapList.size());
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            if (selectConfig.toastHelper != null) {
                selectConfig.toastHelper.showToast("抱歉,没有图片");
            }
        }
    }

    public void showLoading(String tip) {
        dialog = new LoadingDialog(MyIMGEditActivity.this);
        dialog.setMsg(tip);
        dialog.setNotCancel();  //设置dialog不自动消失
        dialog.setCancelable(false);
        dialog.show();
    }

    public void hideLoading() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


    public List<Bitmap> getBitmapList() {
        if (imageLocList != null && imageLocList.size() > 0) {
            for (int i = 0; i < imageLocList.size(); i++) {
                Bitmap bitmap = null;
                if (SystemUtils.beforeAndroidTen()) {
                    bitmap = FileUtil.getBitmap(imageLocList.get(i).path);
                } else {
                    try {
                        if (FileUtils.isFileExists(imageLocList.get(i).path)) bitmap = BitmapFactory.decodeFile(imageLocList.get(i).path);
                        else bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageLocList.get(i).getUri());
//                        bitmap = BitmapFactory.decodeFile(imageLocList.get(i).path);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //bitmap = new CompressHelper.Builder(this).setQuality(selectConfig.maxSize).build().compressToBitmapByUri(imageLocList.get(i).getUri());
                }
                bitmapList.add(bitmap);
            }
            return bitmapList;
        } else {
            return null;
        }
    }


    public void updateModeUI() {
        IMGMode mode = mImgView.getMode();
        switch (mode) {
            case SHADE:
                mModeGroup.check(R.id.rb_shade);
                setOpSubDisplay(OP_SUB_SHADE);
                break;
            case DOODLE:
                mModeGroup.check(R.id.rb_doodle);
                setOpSubDisplay(OP_SUB_DOODLE);
                break;
            case MOSAIC:
                mModeGroup.check(R.id.rb_mosaic);
                setOpSubDisplay(OP_SUB_MOSAIC);
                break;
            case NONE:
                mModeGroup.clearCheck();
                break;
        }
    }


    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.rb_shade) {
            onModeClick(IMGMode.SHADE);
            findViewById(R.id.btn_undo).setVisibility(View.GONE);
        } else if (vid == R.id.rb_doodle) {
            onModeClick(IMGMode.DOODLE);
        } else if (vid == R.id.rb_mosaic) {
            onModeClick(IMGMode.MOSAIC);
        } else if (vid == R.id.btn_text) {
            //onModeClick(IMGMode.NONE);
            onTextModeClick();
            findViewById(R.id.btn_undo).setVisibility(View.GONE);
        } else if (vid == R.id.btn_undo) {
            onUndoClick();
        } else if (vid == R.id.tv_done) {//完成
            //保存时候 保存图片
            for (int i = 0; i < imgViewList.size(); i++) {
                bitmapList.set(i, imgViewList.get(i).saveBitmap());
            }
            onDoneClick();

        } else if (vid == R.id.tv_cancel) {
            onCancelClick();
        }
    }


    public void onTextModeClick() {
        if (mTextDialog == null) {
            mTextDialog = new IMGTextEditDialog(this, this);
            mTextDialog.setWaterMarkTextSize(waterMarkTextSize);
            mTextDialog.setOnShowListener(this);
            mTextDialog.setOnDismissListener(this);
        }
        mTextDialog.show();
    }

    /**
     * 设置viewPager是否可以左右滑动
     *
     * @param isCanScroll
     */
    public void setViewPagerIsCanScroll(Boolean isCanScroll) {
        mViewPager.setIsCanScroll(isCanScroll);
    }

    @Override
    public void onText(IMGText text) {
        mImgView.addStickerText(text, false);
    }

    public void onModeClick(IMGMode mode) {
        IMGMode cm = mImgView.getMode();
        if (cm == mode) {
            setViewPagerIsCanScroll(true);
            mode = IMGMode.NONE;
        } else {
            setViewPagerIsCanScroll(false);
        }
        mImgView.setMode(mode);
        updateModeUI();
    }

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

    /**
     * 返回
     */
    public void onCancelClick() {
        // deletePic();//删除图片
        showBackTip();
    }

    /**
     * 保存
     */
    public void onDoneClick() {
        showLoading("正在处理图片中,请稍等...");
        new Thread(new Runnable() {
            Message msg = Message.obtain();

            @Override
            public void run() {
                saveImageItem();
                msg.what = 0x102;
                if (mHandler != null) {
                    mHandler.sendMessage(msg);
                }
            }
        }).start();

    }

    /**
     * 删除原图片 保存编辑后的图片
     */
    private void saveImageItem() {
        imageSelectList.clear();
        if (isSingleTakePhoto) {
            for (int i = 0; i < bitmapList.size(); i++) {
                if (SystemUtils.beforeAndroidTen()) {
                    imageLocList.get(i).path = FileUtil.saveBitmap(FileUtil.PIC_EDIT_FOLDER_NAME, bitmapList.get(i), this);
                } else {
                    imageLocList.get(i).path = FileUtil.saveBitmapAndroidQ(this, FileUtil.PIC_EDIT_FOLDER_NAME, bitmapList.get(i));
                }
//                if (selectConfig.isCompress) {//进行压缩
//                    BitmapUtils.doRecycledIfNot(bitmapList.get(i));
//                }
            }
        } else {
            imageItemList.clear();
            for (int i = 0; i < imageLocList.size(); i++) {
                imageSelectList.add(imageLocList.get(i).path);
            }
            for (int i = 0; i < bitmapList.size(); i++) {
                if (SystemUtils.beforeAndroidTen()) {
                    imageLocList.get(i).path = FileUtil.saveBitmap(FileUtil.PIC_EDIT_FOLDER_NAME, bitmapList.get(i), this);
                } else {
                    imageLocList.get(i).path = FileUtil.saveBitmapAndroidQ(this, FileUtil.PIC_EDIT_FOLDER_NAME, bitmapList.get(i));
                }
//                if (selectConfig.isCompress) {//进行压缩
//                    BitmapUtils.doRecycledIfNot(bitmapList.get(i));
//                }
            }
            for (int i = 0; i < imageSelectList.size(); i++) {//将新拍摄的图片
                if (!imageSelectList.get(i).contains(FileUtil.PIC_EDIT_FOLDER_NAME) && isDeleteOriginalPic) {
                    FileUtil.deletePic(getApplication(), imageSelectList.get(i));//删除原图(未被编辑过的)
                }
                if (imageSelectList.get(i).contains(FileUtil.PIC_EDIT_FOLDER_NAME) && isDeleteBeforeEditlPic) {
                    FileUtil.deletePic(getApplication(), imageSelectList.get(i));//删除原图(曾经被编辑过的)
                }
            }
            imageItemList.addAll(imageHttpList);
        }
        imageItemList.addAll(imageLocList);
        imageItemList = ImagePicker.transitArray(this, imageItemList);
    }


    public void onColorChanged(int checkedColor) {
        if (mImgView != null) mImgView.setPenColor(checkedColor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Bitmap bitmap : bitmapList) {
            if (bitmap != null) bitmap.recycle();
        }
        if (imageItemList != null) {
            imageItemList.clear();
        }
        if (dialog != null) {
            dialog = null;
        }
    }

    @Override
    public final void onCheckedChanged(RadioGroup group, int checkedId) {
        onColorChanged(mColorGroup.getCheckColor());
    }


    @Override
    public void onShow(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.GONE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mModeGroup.check(R.id.rb_shade);
        mOpSwitcher.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        showBackTip();
    }

    public void showBackTip() {
        new AlertDialog.Builder(this).setTitle("是否退出").setMessage("返回后修改的数据将不会自动保存").setPositiveButton("继续编辑", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        }).show();

    }


}


