package com.ypx.imagepicker.editLibrary;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
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
import com.ypx.imagepicker.utils.BitmapUtils;

import java.io.File;
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
    private TextView tvDone;
    private String waterMark;
    private LoadingDialog dialog;
    private String waterMarkColor;
    private MultiSelectConfig selectConfig;
    private boolean isDeleteOriginalPic = false; //是否删除原图
    private boolean isDeleteBeforeEditPic = false;//是否删除编辑后的图片
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

    private String imageSavePath = FileUtil.PIC_EDIT_FOLDER_NAME;

    private boolean save2DCIM = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_edit_vp_activity);
        selectConfig = (MultiSelectConfig) getIntent().getSerializableExtra(Config.CONGIG);
        if (selectConfig != null) {
            isSingleTakePhoto = selectConfig.isSingleTakePhoto();
            waterMark = selectConfig.getWaterMark();
            imageSavePath = selectConfig.getImageSavePath();
            save2DCIM = selectConfig.isSave2DCIM();
            waterMarkTextSize = selectConfig.getWaterMarkTextSize();
            waterMarkColor = selectConfig.getWaterMarkColor();
            isDeleteOriginalPic = selectConfig.isDeleteOriginalPic();
            isDeleteBeforeEditPic = selectConfig.isDeleteBeforeEditPic();
        }
        initViews();
        imageItemList = (ArrayList<ImageItem>) getIntent().getSerializableExtra(ImagePicker.INTENT_KEY_PICKER_RESULT);
        initData();
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
        int defaultShadowColor = Color.parseColor(selectConfig.defaultShadowColor);
        for (int i = 0; i < mColorGroup.getChildCount(); i++) {
            IMGColorRadio colorRadio = ((IMGColorRadio) mColorGroup.getChildAt(i));
            if (colorRadio.getColor() == defaultShadowColor) {
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

    Handler mHandler = new Handler(msg -> {
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
    });

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

            if (!TextUtils.isEmpty(waterMark) && !imageItemList.get(position).path.contains(imageSavePath)) {
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
        if (imageItemList != null && imageItemList.size() > 0) {
            tvPage.setText(1 + " / " + imageItemList.size());
            imgViewList = new ArrayList<>();
            int penColor = Color.WHITE;
            if (selectConfig.defaultShadowColor != null && selectConfig.defaultShadowColor.length() > 0) {
                penColor = Color.parseColor(selectConfig.getDefaultShadowColor());
            } else {
                penColor = ((IMGColorRadio) mColorGroup.getChildAt(0)).getColor();
            }
            for (ImageItem item : imageItemList) {
                IMGView imgView = new IMGView(this);
//                imgView.setPenColor(ImagePicker.getEditPicPenColor());
                imgView.setPenColor(penColor);
                imgView.setSelectStatusListener(selectStatusListener);
                imgViewList.add(imgView);
            }
            preInitBitmap(0);
            mImgView = imgViewList.get(0);
            mImgView.setImageBitmap(((Bitmap) mImgView.getTag()));


//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    for (int i = 0; i < imgViewList.size(); i++) {
//                        if (imgViewList.get(i).getTag() == null) {
//                            imgViewList.get(i).setTag(getBitmap(i));
//                        }
//                    }
//                }
//            }).start();


            MyAdapter vpAdapter = new MyAdapter(imgViewList);
            mViewPager.setAdapter(vpAdapter);
            mViewPager.setOffscreenPageLimit(imgViewList.size() + 1);
            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    mImgView = imgViewList.get(position);
                    preInitBitmap(position);
                    mImgView.setImageBitmap(((Bitmap) mImgView.getTag()));

                    preInitBitmap(position + 1);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvPage.setText(position + 1 + " / " + imageItemList.size());
                        }
                    });
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            mViewPager.setCurrentItem(0);
        } else {
            if (selectConfig.toastHelper != null) {
                selectConfig.toastHelper.showToast("抱歉,没有图片");
            }
        }
    }

    private void preInitBitmap(int position) {
        if (imgViewList.size() > position && imgViewList.get(position).getTag() == null) {
            imgViewList.get(position).setTag(getBitmap(position));
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

    public Bitmap getBitmap(int i) {
        Bitmap bitmap = null;
        if (SystemUtils.beforeAndroidTen()) {
            bitmap = FileUtil.getBitmap(imageItemList.get(i).path);
        } else {
            try {
                if (FileUtils.isFileExists(imageItemList.get(i).path)) bitmap = BitmapFactory.decodeFile(imageItemList.get(i).path);
                else bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageItemList.get(i).getUri());
//                        bitmap = BitmapFactory.decodeFile(imageLocList.get(i).path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //bitmap = new CompressHelper.Builder(this).setQuality(selectConfig.maxSize).build().compressToBitmapByUri(imageLocList.get(i).getUri());
        }
        return bitmap;
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
                //保存时候 保存图片
                for (int i = 0; i < imgViewList.size(); i++) {
                    mViewPager.setCurrentItem(i);
                    if (imgViewList.get(i).getTag() == null) {
                        imgViewList.get(i).setTag(getBitmap(i));
                    }
                    /*被回收的说明已经保存过*/
//                    if (!((Bitmap) imgViewList.get(i).getTag()).isRecycled()) {
                    saveBitmap2File(imageItemList.get(i), imgViewList.get(i).saveBitmap());
                    imgViewList.get(i).mImage.release();
//                    }
                }
                deleteFile();
                msg.what = 0x102;
                if (mHandler != null) {
                    mHandler.sendMessage(msg);
                }
            }
        }).start();

    }

    private String getImageSavePath() {
        if (imageSavePath.endsWith(File.separator)) return imageSavePath;
        else return imageSavePath + File.separator;
    }

    private void saveBitmap2File(ImageItem item, Bitmap bitmap) {
        if (save2DCIM) {
            if (SystemUtils.beforeAndroidTen()) {
                item.path = FileUtil.saveBitmap(imageSavePath, bitmap, this);
            } else {
                item.path = FileUtil.saveBitmapAndroidQ(this, imageSavePath, bitmap);
            }
        } else {
            String filePath = getImageSavePath() + System.currentTimeMillis() + ".jpg";
            item.path = filePath;
            BitmapUtils.saveFile(filePath, bitmap);
            updateFileFromDatabase(filePath);
        }
    }


    private void updateFileFromDatabase(String filepath) {
        MediaScannerConnection.scanFile(this, new String[]{filepath}, null, null);
    }

    /**
     * 删除原图片 保存编辑后的图片
     */
    private void deleteFile() {
        for (int i = 0; i < imageItemList.size(); i++) {//将新拍摄的图片
            if (!imageItemList.get(i).path.contains(imageSavePath) && isDeleteOriginalPic) {
                FileUtil.deletePic(getApplication(), imageItemList.get(i).path);//删除原图(未被编辑过的)
            }
            if (imageItemList.get(i).path.contains(imageSavePath) && isDeleteBeforeEditPic) {
                FileUtil.deletePic(getApplication(), imageItemList.get(i).path);//删除原图(曾经被编辑过的)
            }
        }
    }


    public void onColorChanged(int checkedColor) {
        if (mImgView != null) mImgView.setPenColor(checkedColor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (IMGView imgView : imgViewList) {
            imgView.mImage.release();
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
        new AlertDialog.Builder(this).setTitle("是否退出").setMessage("返回后修改的数据将不会自动保存").setPositiveButton("继续编辑", (dialog, which) -> dialog.dismiss()).setNegativeButton("退出", (dialog, which) -> {
            dialog.dismiss();
            finish();
        }).show();

    }


}


