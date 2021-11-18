package com.ypx.imagepicker.cameralibrary;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.ypx.imagepicker.ImagePicker;
import com.ypx.imagepicker.R;
import com.ypx.imagepicker.activity.MyAppActivity;
import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.bean.selectconfig.MultiSelectConfig;
import com.ypx.imagepicker.cameralibrary.listener.ClickListener;
import com.ypx.imagepicker.cameralibrary.listener.ErrorListener;
import com.ypx.imagepicker.cameralibrary.listener.JCameraListener;
import com.ypx.imagepicker.config.Config;
import com.ypx.imagepicker.constant.Code;
import com.ypx.imagepicker.editLibrary.MyIMGEditActivity;
import com.ypx.imagepicker.editLibrary.utils.FileUtil;
import com.ypx.imagepicker.editLibrary.utils.SystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ren.perry.perry.LoadingDialog;


public class EasyCameraActivity extends AppCompatActivity {

    private JCameraView jCameraView;
    private ProgressBar pbProgress;
    private RelativeLayout rlCoverView;

    private String cameraPath = null;
    private static String storagePath = "";
    private static final File parentPath = Environment.getExternalStorageDirectory();
    public static Bitmap bitmapImg;
    private String number;
    private int maxNum = 4;
    private MultiSelectConfig selectConfig;
    private List<Bitmap> bitmapList;
    private LoadingDialog dialog;
    private ArrayList<ImageItem> imageItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectConfig = (MultiSelectConfig) getIntent().getSerializableExtra(Config.CONGIG);
        toCustomCamera();

    }


    private void toCustomCamera() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setContentView(R.layout.activity_camera);
        pbProgress = findViewById(R.id.pb_progress);
        jCameraView = findViewById(R.id.jCameraView);
        jCameraView.enableCameraTip(true);
        jCameraView.setSelectConfig(selectConfig);
        jCameraView.setMaxNum(selectConfig.imgMaxNum);

        initCustomCamera();
    }

    private int getFeature() {
        return JCameraView.BUTTON_STATE_ONLY_CAPTURE;//拍照
    }

    private void initCustomCamera() {
        //视频存储路径
//        if (SystemUtils.beforeAndroidTen()) {
//            jCameraView.setSaveVideoPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator );
//        } else {
//            jCameraView.setSaveVideoPath(getExternalFilesDir(Environment.DIRECTORY_DCIM) + File.separator );
//        }
        jCameraView.setFeatures(getFeature());
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);
        //fixme 录像时间+800ms 修复录像时间少1s问题
        //  jCameraView.setDuration(15800);
        jCameraView.setErrorListener(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
                //Toast.makeText(EasyCameraActivity.this, getString(R.string.missing_audio_permission), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        jCameraView.setJCameraListener(new JCameraListener() {
            @Override
            public void captureSuccess(List<Bitmap> bitmaps) {

                bitmapList = bitmaps;
                if (selectConfig.isCanEditPic()) {
                    MyAppActivity.setBitmapList(bitmaps);
                    Intent intent = new Intent(EasyCameraActivity.this, MyIMGEditActivity.class);
                    intent.putExtra(Config.CONGIG, selectConfig);
                    startActivityForResult(intent, Code.REQUEST_EDIT);
                } else {
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


            }

            @Override
            public void back() {
                finish();
            }

            @Override
            public void recordSuccess(final String url, Bitmap firstFrame) {
//                //获取视频路径
//                if (SystemUtils.beforeAndroidTen()) {
//                    //String path = FileUtil.saveBitmap(applicationName, firstFrame);
//                    Intent intent = new Intent();
//                    intent.putExtra(Key.EXTRA_RESULT_CAPTURE_VIDEO_PATH, url);
//                    setResult(RESULT_OK, intent);
//                    finish();
//                } else {
//                    pbProgress.setVisibility(View.VISIBLE);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            //String path = FileUtil.saveBitmap(applicationName, firstFrame);
//                            final String resUrl = FileUtil.copy2DCIMAndroidQ(EasyCameraActivity.this, url, applicationName);
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    pbProgress.setVisibility(View.GONE);
//                                    if (!isFinishing()) {
//                                        Intent intent = new Intent();
//                                        //intent.putExtra(Key.EXTRA_RESULT_CAPTURE_IMAGE_PATH, path);
//                                        intent.putExtra(Key.EXTRA_RESULT_CAPTURE_VIDEO_PATH, resUrl);
//                                        setResult(RESULT_OK, intent);
//                                        finish();
//                                    }
//                                }
//                            });
//                        }
//                    }).start();
//                }

            }
        });

        jCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
    }


    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0x102) {
                hideLoading();
                Intent intent = new Intent();
                intent.putExtra(ImagePicker.INTENT_KEY_PICKER_RESULT, imageItemList);
                setResult(ImagePicker.REQ_PICKER_RESULT_CODE, intent);
                finish();

            }

            return false;
        }
    });

    /**
     * 删除原图片 保存编辑后的图片
     */
    private void saveImageItem() {
        imageItemList = new ArrayList<>();
        initImageBitmap();
        for (int i = 0; i < bitmapList.size(); i++) {
            if (SystemUtils.beforeAndroidTen()) {
                imageItemList.get(i).path = FileUtil.saveBitmap(FileUtil.PIC_FOLDER_NAME, bitmapList.get(i), this);
            } else {
                imageItemList.get(i).path = FileUtil.saveBitmapAndroidQ(this, FileUtil.PIC_FOLDER_NAME, bitmapList.get(i));
            }
        }
        imageItemList = ImagePicker.transitArray(this, imageItemList);
    }


    /**
     *
     */
    private void initImageBitmap() {
        for (Bitmap imageItem : bitmapList) {
            imageItemList.add(new ImageItem(""));
        }
    }

    public void showLoading(String tip) {
        dialog = new LoadingDialog(EasyCameraActivity.this);
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


    public List<byte[]> getBitmapByteList(List<Bitmap> bitmapList) {
        List<byte[]> bitmapByteList = new ArrayList<>();
        for (Bitmap bitmap : bitmapList) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] bitmapByte = baos.toByteArray();
                bitmapByteList.add(bitmapByte);
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bitmapByteList;
    }

    private String initPath() {
        if (storagePath.equals("")) {
            storagePath = parentPath.getAbsolutePath() + File.separator + "myphoto";
            File file = new File(storagePath);
            if (!file.exists()) {
                file.mkdir();
            }
        }
        return storagePath;
    }

    public String saveEditorPhotoJpgPath() {
        return initPath() + File.separator + "editor_" + System.currentTimeMillis() + ".jpg";
    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // if (!Setting.useSystemCamera) {
        jCameraView.onResume();
//        if (MyAppActivity.getBitmapList() != null) {
//            jCameraView.resetBitmap(MyAppActivity.getBitmapList());
        //}
        //}
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if (!Setting.useSystemCamera) {
        jCameraView.onPause();
        //}
    }

    @Override
    protected void onDestroy() {
        // if (Setting.cameraCoverView != null) Setting.cameraCoverView.clear();
        // Setting.cameraCoverView = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Code.REQUEST_EDIT && Code.REQUEST_EDIT == requestCode) {
            ArrayList<ImageItem> imageItemList = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.INTENT_KEY_PICKER_RESULT);
            Intent intent = new Intent();
            intent.putExtra(ImagePicker.INTENT_KEY_PICKER_RESULT, imageItemList);
            setResult(ImagePicker.REQ_PICKER_RESULT_CODE, intent);
            finish();
        }
    }
}
