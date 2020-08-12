package com.ypx.imagepicker.editLibrary;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ypx.imagepicker.ImagePicker;
import com.ypx.imagepicker.R;
import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.config.Config;
import com.ypx.imagepicker.editLibrary.core.IMGMode;
import com.ypx.imagepicker.editLibrary.core.IMGText;
import com.ypx.imagepicker.editLibrary.utils.FileUtil;
import com.ypx.imagepicker.editLibrary.utils.SystemUtils;
import com.ypx.imagepicker.editLibrary.view.IMGColorGroup;
import com.ypx.imagepicker.editLibrary.view.IMGView;
import com.ypx.imagepicker.helper.recyclerviewitemhelper.SimpleItemTouchHelperCallback;
import com.ypx.imagepicker.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ren.perry.perry.LoadingDialog;

/**
 * time：2020/7/23
 * author：pachy1990
 * 描述：
 */
public class MyIMGEditActivity extends Activity implements View.OnClickListener,
        IMGTextEditDialog.Callback, RadioGroup.OnCheckedChangeListener,
        DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    protected IMGView mImgView;

    private RadioGroup mModeGroup;

    private IMGColorGroup mColorGroup;

    private IMGTextEditDialog mTextDialog;

    private View mLayoutOpSub;

    private ViewSwitcher mOpSwitcher, mOpSubSwitcher;

    public static final int OP_HIDE = -1;

    public static final int OP_NORMAL = 0;

    public static final int OP_CLIP = 1;

    public static final int OP_SUB_DOODLE = 0;

    public static final int OP_SUB_MOSAIC = 1;
    public static final int OP_SUB_SHADE = 0;
    private RecyclerView mPreviewRecyclerView;
    private MultiPreviewEditAdapter previewAdapter;
    public ArrayList<ImageItem> imageItemList = new ArrayList<>();
    public ArrayList<ImageItem> imageLocList = new ArrayList<>();
    public ArrayList<ImageItem> imageHttpList = new ArrayList<>();
    public List<Bitmap> bitmapList = new ArrayList<>();
    public List<Integer> noCompleteList = new ArrayList<>();
    private int lastSelectPos = 0;
    private List<Integer> selectPosList;
    private FrameLayout frameLayout;
    private int newSelectPos = 0;
    private TextView tvDone;
    private String btnContent;
    private String number;
    private LoadingDialog dialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageItemList = (ArrayList<ImageItem>) getIntent().getSerializableExtra(ImagePicker.INTENT_KEY_PICKER_RESULT);
        number = getIntent().getStringExtra(Config.CONGIG_SHOW_NUMBER);
        setContentView(R.layout.image_edit_activity);
        initImage();
        initViews();
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
    }

    /**
     * 处理排除网络图片
     */
    private void initImage() {
        for (ImageItem imageItem : imageItemList) {
            if (imageItem.path.startsWith("http")) {
                imageHttpList.add(imageItem);
            } else {
                imageLocList.add(imageItem);
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
                ImagePicker.closePickerWithCallback(imageItemList);
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

    private void initData() {
        if (bitmapList != null && bitmapList.size() > 0) {
            selectPosList = new ArrayList<>();
            previewAdapter.setListData(bitmapList);
            mImgView.setImageBitmap(bitmapList.get(lastSelectPos));
            selectPosList.add(lastSelectPos);
            if (!TextUtils.isEmpty(number)) {
                if (!imageLocList.get(lastSelectPos).path.contains(FileUtil.PIC_EDIT_FOLDER_NAME) && !imageLocList.get(0).path.startsWith("http")) {
                    mImgView.addStickerText(new IMGText(number, Color.RED), true);
                    noCompleteList.add(0);
                }
            }
//            if (bitmapList.size()==1){
//                btnContent = "完成";
//                setBtnTextView(btnContent);
//            }else{
//                btnContent = "下一张";
//                setBtnTextView(btnContent);
//            }
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
                if (!imageLocList.get(i).path.startsWith("http")) {
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageLocList.get(i).getUri());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bitmapList.add(bitmap);
                }
            }

            return bitmapList;

        } else {
            return null;
        }


    }

    /**
     * 选中的图片添加到自定义控件中等待编辑操作
     *
     * @param lastSelectPos
     * @param selectPos
     */
    public void setmImgView(int lastSelectPos, int selectPos) {
        if (lastSelectPos == selectPos) {
            return;
        }
        //    this. lastSelectPos = lastSelectPos;
        this.newSelectPos = selectPos;
        bitmapList.set(lastSelectPos, mImgView.saveBitmap());
        if (!noCompleteList.contains(newSelectPos)) {
            noCompleteList.add(newSelectPos);
        }
        mImgView.setClearCanvas();
        mImgView.setImageBitmap(bitmapList.get(selectPos));
        if (!selectPosList.contains(selectPos)) {
            //编码不为空时并且不是网络图片,并且此图片之前未被编辑过,设置显示编码
            if (!TextUtils.isEmpty(number)
                    && !imageLocList.get(selectPos).path.contains(FileUtil.PIC_EDIT_FOLDER_NAME)
                    && !imageLocList.get(selectPos).path.startsWith("http")) {
                mImgView.addStickerText(new IMGText(number, Color.RED), true);
            }
            selectPosList.add(selectPos);
        }

//        if (bitmapList.size()==newSelectPos){
//            btnContent = "完成";
//            setBtnTextView(btnContent);
//        }else{
//            btnContent = "下一张";
//            setBtnTextView(btnContent);
//        }
//        this. lastSelectPos = newSelectPos;

    }


    private void initViews() {
        tvDone = findViewById(R.id.tv_done);
        mImgView = findViewById(R.id.image_canvas);
        mModeGroup = findViewById(R.id.rg_modes);

        mOpSwitcher = findViewById(R.id.vs_op);
        mOpSubSwitcher = findViewById(R.id.vs_op_sub);

        mColorGroup = findViewById(R.id.cg_colors);
        mColorGroup.setOnCheckedChangeListener(this);

        mLayoutOpSub = findViewById(R.id.layout_op_sub);
        mPreviewRecyclerView = findViewById(R.id.mPreviewRecyclerView);
        initPreviewList();

    }

    private void initPreviewList() {
        mPreviewRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        previewAdapter = new MultiPreviewEditAdapter(bitmapList);
        mPreviewRecyclerView.setAdapter(previewAdapter);
        SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(previewAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mPreviewRecyclerView);
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
            case CLIP:
                mImgView.setMode(IMGMode.NONE);
                mModeGroup.clearCheck();
                setOpSubDisplay(OP_HIDE);
                setOpDisplay(OP_CLIP);
                break;
            case NONE:
                mModeGroup.clearCheck();
                setOpSubDisplay(OP_HIDE);
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
        } else if (vid == R.id.btn_text) {
            onModeClick(IMGMode.NONE);
            onTextModeClick();
        } else if (vid == R.id.rb_mosaic) {
            onModeClick(IMGMode.MOSAIC);
        } else if (vid == R.id.btn_clip) {
            onModeClick(IMGMode.CLIP);
        } else if (vid == R.id.btn_undo) {
            onUndoClick();
        } else if (vid == R.id.tv_done) {//完成
//            if (isFastDoubleClick()) {
//                return;
//            }
            // if (newSelectPos == bitmapList.size()-1) {


            //保存时候 把最后选中的图片保存一下
            bitmapList.set(newSelectPos, mImgView.saveBitmap());

            onDoneClick();

//            } else {
//                newSelectPos++;
//                previewAdapter.setSelectPos(newSelectPos);
//                setmImgView(lastSelectPos,newSelectPos);
//                lastSelectPos =  newSelectPos;
//
//                if (bitmapList.size()-1==newSelectPos){
//                    btnContent = "完成";
//                    setBtnTextView(btnContent);
//                }else{
//                    btnContent = "下一张";
//                    setBtnTextView(btnContent);
//                }
//            }


        } else if (vid == R.id.tv_cancel) {
            onCancelClick();
        } else if (vid == R.id.ib_clip_cancel) {
            onCancelClipClick();
        } else if (vid == R.id.ib_clip_done) {
            onDoneClipClick();
        } else if (vid == R.id.tv_clip_reset) {
            onResetClipClick();
        } else if (vid == R.id.ib_clip_rotate) {
            onRotateClipClick();
        }
    }


    public void onTextModeClick() {
        if (mTextDialog == null) {
            mTextDialog = new IMGTextEditDialog(this, this);
            mTextDialog.setOnShowListener(this);
            mTextDialog.setOnDismissListener(this);
        }
        mTextDialog.show();
    }


    @Override
    public void onText(IMGText text) {
        mImgView.addStickerText(text, false);
    }

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

    public void onCancelClick() {
        // deletePic();//删除图片
        finish();
    }

    public void onDoneClick() {
        if (!TextUtils.isEmpty(number)) {
            for (int i = 0; i < imageLocList.size(); i++) {
                if (!imageLocList.get(i).path.contains(FileUtil.PIC_EDIT_FOLDER_NAME) && !noCompleteList.contains(i)) {
                    int postion = i + 1;
                    ToastUtils.showToastError(getApplicationContext(), "请编辑第" + postion + "张图片!");
                    return;
                }
            }
        }

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

        for (int i = 0; i < imageLocList.size(); i++) {
//            if (!imageItemList.get(i).path.startsWith("http")) {
                FileUtil.deletePic(getApplication(), imageLocList.get(i).path);
//            }
        }

        for (int i = 0; i < bitmapList.size(); i++) {
            if (SystemUtils.beforeAndroidTen()) {
                imageItemList.get(i).path = FileUtil.saveBitmap(FileUtil.PIC_EDIT_FOLDER_NAME, bitmapList.get(i));
            } else {
                imageItemList.get(i).path = FileUtil.saveBitmapAndroidQ(this, FileUtil.PIC_EDIT_FOLDER_NAME, bitmapList.get(i));
            }
        }
        imageItemList.clear();
        imageItemList.addAll(imageHttpList);
        imageItemList.addAll(imageLocList);
        imageItemList = ImagePicker.transitArray(this, imageItemList);
    }


    public void onCancelClipClick() {
        mImgView.cancelClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }


    public void onDoneClipClick() {
        mImgView.doClip();
        setOpDisplay(mImgView.getMode() == IMGMode.CLIP ? OP_CLIP : OP_NORMAL);
    }


    public void onResetClipClick() {
        mImgView.resetClip();
    }


    public void onRotateClipClick() {
        mImgView.doRotate();
    }


    public void onColorChanged(int checkedColor) {
        mImgView.setPenColor(checkedColor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Bitmap bitmap : bitmapList) {
            bitmap.recycle();
            bitmap = null;
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

    public void setOpDisplay(int op) {
        if (op >= 0) {
            mOpSwitcher.setDisplayedChild(op);
        }
    }

    public void setOpSubDisplay(int opSub) {
        if (opSub < 0) {
            mLayoutOpSub.setVisibility(View.GONE);
        } else {
            mOpSubSwitcher.setDisplayedChild(opSub);
            mLayoutOpSub.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onShow(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.GONE);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mOpSwitcher.setVisibility(View.VISIBLE);
    }

}
