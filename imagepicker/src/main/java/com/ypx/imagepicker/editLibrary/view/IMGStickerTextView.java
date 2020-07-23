package com.ypx.imagepicker.editLibrary.view;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.ypx.imagepicker.editLibrary.IMGTextEditDialog;
import com.ypx.imagepicker.editLibrary.core.IMGText;


/**
 * Created by felix on 2017/11/14 下午7:27.
 */
public class IMGStickerTextView extends IMGStickerView implements IMGTextEditDialog.Callback {

    private static final String TAG = "IMGStickerTextView";

    private TextView mTextView;

    private IMGText mText;

    private IMGTextEditDialog mDialog;

    private static float mBaseTextSize = -1f;

    public static final int PADDING = 26;

    private static final float TEXT_SIZE_SP = 6f;

    public IMGStickerTextView(Context context) {
        this(context, null, 0);
    }

    public IMGStickerTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IMGStickerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onInitialize(Context context) {
        if (mBaseTextSize <= 0) {
            mBaseTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    TEXT_SIZE_SP, context.getResources().getDisplayMetrics());
        }
        super.onInitialize(context);
    }

    @Override
    public View onCreateContentView(Context context) {
        mTextView = new TextView(context);
        mTextView.setTextSize(mBaseTextSize);
        mTextView.setPadding(PADDING, PADDING, PADDING, PADDING);
        mTextView.setTextColor(Color.WHITE);

        return mTextView;
    }

    public void setText(IMGText text) {
        mText = text;
        if (mText != null && mTextView != null) {
            mTextView.setText(mText.getText());
            mTextView.setTextColor(mText.getColor());

           // mTextView.setBackgroundColor(Color.BLUE);
        }
    }
    public void setShadeText(int color,int width,int height) {
       mText =new IMGText("",Color.RED);
        if (mText != null && mTextView != null) {
            mTextView.setWidth(width+52);
            mTextView.setHeight(height+52);
            mTextView.setBackgroundColor(color);

        }
    }

    public IMGText getText() {
        return mText;
    }

    @Override
    public void onContentTap() {
        if (TextUtils.isEmpty(mText.getText())){
            return;
        }
        IMGTextEditDialog dialog = getDialog();
        dialog.setText(mText);
        dialog.show();
    }

    private IMGTextEditDialog getDialog() {
        if (mDialog == null) {
            mDialog = new IMGTextEditDialog(getContext(), this);
        }
        return mDialog;
    }

    @Override
    public void onText(IMGText text) {
        mText = text;
        if (mText != null && mTextView != null) {
            mTextView.setText(mText.getText());
            mTextView.setTextColor(mText.getColor());
        }
    }
}