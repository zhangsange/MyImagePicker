package com.ypx.imagepicker.editLibrary;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.editLibrary.utils.FileUtil;
import com.ypx.imagepicker.helper.recyclerviewitemhelper.SimpleItemTouchHelperCallback;
import com.ypx.imagepicker.R;
import com.ypx.imagepicker.editLibrary.core.IMGMode;
import com.ypx.imagepicker.editLibrary.core.IMGText;
import com.ypx.imagepicker.editLibrary.view.IMGColorGroup;
import com.ypx.imagepicker.editLibrary.view.IMGView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by felix on 2017/12/5 下午3:08.
 */

abstract class IMGEditBaseActivity extends Activity implements View.OnClickListener,
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
    public List<Bitmap> bitmapList = new ArrayList<>();
    private int lastSelectPos = 0;
    private List<Integer> selectPosList;
    private FrameLayout frameLayout;
    private int newSelectPos = 0;
    private TextView tvDone;
    private String btnContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageItemList = imageItemList();
        bitmapList = getBitmapList();

        if (bitmapList != null && bitmapList.size() > 0) {
            selectPosList = new ArrayList<>();
            setContentView(R.layout.image_edit_activity);
            initViews();
            mImgView.setImageBitmap(bitmapList.get(lastSelectPos));
            selectPosList.add(lastSelectPos);
            if (!TextUtils.isEmpty(getNumber())) {
                if (!imageItemList.get(lastSelectPos).path.contains(FileUtil.PIC_EDIT_FOLDER_NAME) && !imageItemList.get(0).path.startsWith("http")) {
                    mImgView.addStickerText(new IMGText(getNumber(), Color.RED), true);
                }
            }
//            if (bitmapList.size()==1){
//                btnContent = "完成";
//                setBtnTextView(btnContent);
//            }else{
//                btnContent = "下一张";
//                setBtnTextView(btnContent);
//            }
//            onCreated();
        }
    }

    protected abstract String getNumber();
    public void setBtnTextView(String content){
        tvDone.setText(content);
    }

    public void setmImgView(int lastSelectPos, int selectPos) {
        if (lastSelectPos == selectPos) {
            return;
        }
   //    this. lastSelectPos = lastSelectPos;
       this. newSelectPos = selectPos;
        bitmapList.set(lastSelectPos, mImgView.saveBitmap());
        mImgView.setClearCanvas();
        mImgView.setImageBitmap(bitmapList.get(selectPos));
        if (!selectPosList.contains(selectPos)) {
            if (!TextUtils.isEmpty(getNumber())
                    && !imageItemList.get(selectPos).path.contains(FileUtil.PIC_EDIT_FOLDER_NAME)
                    && !imageItemList.get(selectPos).path.startsWith("http")) {
                mImgView.addStickerText(new IMGText(getNumber(), Color.RED), true);
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
        } else if (vid == R.id.tv_done) {
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


    private long lastClickTime = 0;

    public boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
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

    public void onTextModeClick() {
        if (mTextDialog == null) {
            mTextDialog = new IMGTextEditDialog(this, this);
            mTextDialog.setOnShowListener(this);
            mTextDialog.setOnDismissListener(this);
        }
        mTextDialog.show();
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

    public abstract List<Bitmap> getBitmapList();

    public abstract ArrayList<ImageItem> imageItemList();

    public abstract void onModeClick(IMGMode mode);

    public abstract void onUndoClick();

    public abstract void onCancelClick();

    public abstract void onDoneClick();

    public abstract void onCancelClipClick();

    public abstract void onDoneClipClick();

    public abstract void onResetClipClick();

    public abstract void onRotateClipClick();

    public abstract void onColorChanged(int checkedColor);

    @Override
    public abstract void onText(IMGText text);
}
