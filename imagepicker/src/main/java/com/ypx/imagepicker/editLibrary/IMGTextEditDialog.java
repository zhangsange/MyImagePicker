package com.ypx.imagepicker.editLibrary;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;

import com.ypx.imagepicker.R;
import com.ypx.imagepicker.editLibrary.core.IMGText;
import com.ypx.imagepicker.editLibrary.view.IMGColorGroup;


/**
 * Created by felix on 2017/12/1 上午11:21.
 */

public class IMGTextEditDialog extends Dialog implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "IMGTextEditDialog";

    private EditText mEditText;

    private Callback mCallback;

    private IMGText mDefaultText;

    private IMGColorGroup mColorGroup;
    private float waterMarkTextSize;

    public IMGTextEditDialog(Context context, Callback callback) {
        super(context, R.style.ImageTextDialog);
        setContentView(R.layout.image_text_dialog);
        mCallback = callback;
        Window window = getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mColorGroup = findViewById(R.id.cg_colors);
        mColorGroup.setOnCheckedChangeListener(this);
        mEditText = findViewById(R.id.et_text);
        showSoftInputFromWindow(mEditText);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.tv_done).setOnClickListener(this);
    }
    public void showSoftInputFromWindow(EditText editText){
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mDefaultText != null) {
            mEditText.setText(mDefaultText.getText());
            mEditText.setTextColor(mDefaultText.getColor());
            if (!mDefaultText.isEmpty()) {
                mEditText.setSelection(mEditText.length());
            }
            mDefaultText = null;
        } else mEditText.setText("");
        mColorGroup.setCheckColor(mEditText.getCurrentTextColor());
    }

    public  int setAlphaComponent(@ColorInt int color, @IntRange(from = 0L,to = 255L) int alpha) {
        if (alpha >= 0 && alpha <= 255) {
            return color & 16777215 | alpha << 24;
        } else {
            throw new IllegalArgumentException("alpha must be between 0 and 255.");
        }
    }


    public void setText(IMGText text) {
        mDefaultText = text;
    }

    public void reset() {
        setText(new IMGText(null, Color.WHITE));
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.tv_done) {
            onDone();
        } else if (vid == R.id.tv_cancel) {
            dismiss();
        }
    }

    private void onDone() {
        String text = mEditText.getText().toString();
        if (!TextUtils.isEmpty(text) && mCallback != null) {
            mCallback.onText(new IMGText(text,setAlphaComponent(mEditText.getCurrentTextColor(),204),waterMarkTextSize));
        }
        dismiss();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mEditText.setTextColor(mColorGroup.getCheckColor());
    }

    public void setWaterMarkTextSize(float waterMarkTextSize) {
        this.waterMarkTextSize = waterMarkTextSize;
    }

    public interface Callback {

        void onText(IMGText text);
    }
}
