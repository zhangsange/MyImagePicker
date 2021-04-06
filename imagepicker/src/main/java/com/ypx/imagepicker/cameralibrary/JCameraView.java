package com.ypx.imagepicker.cameralibrary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ypx.imagepicker.R;
import com.ypx.imagepicker.bean.selectconfig.MultiSelectConfig;
import com.ypx.imagepicker.cameralibrary.listener.ClickListener;
import com.ypx.imagepicker.cameralibrary.listener.ErrorListener;
import com.ypx.imagepicker.cameralibrary.listener.JCameraListener;
import com.ypx.imagepicker.cameralibrary.listener.JCameraPreViewListener;
import com.ypx.imagepicker.cameralibrary.state.CameraMachine;
import com.ypx.imagepicker.cameralibrary.util.FileUtil;
import com.ypx.imagepicker.cameralibrary.util.LogUtil;
import com.ypx.imagepicker.cameralibrary.util.ScreenUtils;
import com.ypx.imagepicker.cameralibrary.view.CameraView;
import com.ypx.imagepicker.editLibrary.MultiPreviewEditAdapter;
import com.ypx.imagepicker.editLibrary.core.util.BitmapUtil;
import com.ypx.imagepicker.helper.recyclerviewitemhelper.SimpleItemTouchHelperCallback;
import com.ypx.imagepicker.utils.BitmapUtils;
import com.ypx.imagepicker.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.0.4
 * 创建日期：2017/4/25
 * 描    述：
 * =====================================
 */
public class JCameraView extends FrameLayout implements CameraInterface.CameraOpenOverCallback, SurfaceHolder.Callback, CameraView {
//    private static final String TAG = "JCameraView";

    //Camera状态机
    private CameraMachine machine;

    //闪关灯状态
    private static final int TYPE_FLASH_AUTO = 0x021;
    private static final int TYPE_FLASH_ON = 0x022;
    private static final int TYPE_FLASH_OFF = 0x023;
    private int type_flash = TYPE_FLASH_OFF;

    //拍照浏览时候的类型
    public static final int TYPE_PICTURE = 0x001;
    public static final int TYPE_VIDEO = 0x002;
    public static final int TYPE_SHORT = 0x003;
    public static final int TYPE_DEFAULT = 0x004;

    //录制视频比特率
    public static final int MEDIA_QUALITY_SUPER = 84 * 100000;
    public static final int MEDIA_QUALITY_HIGH = 52 * 100000;
    public static final int MEDIA_QUALITY_MIDDLE = 28 * 100000;
    public static final int MEDIA_QUALITY_LOW = 14 * 100000;
    public static final int MEDIA_QUALITY_POOR = 8 * 100000;


    public static final int BUTTON_STATE_ONLY_CAPTURE = 0x101;      //只能拍照
    public static final int BUTTON_STATE_ONLY_RECORDER = 0x102;     //只能录像
    public static final int BUTTON_STATE_BOTH = 0x103;              //两者都可以


    //回调监听
    private JCameraListener jCameraLisenter;
    private ClickListener leftClickListener;
    private ClickListener rightClickListener;

    private VideoView mVideoView;
    private ImageView mPhoto;
    private ImageView mSwitchCamera;
    private ImageView mFlashLamp;
    //private CaptureLayout mCaptureLayout;
    private FoucsView mFoucsView;
    private MediaPlayer mMediaPlayer;

    private int layout_width;
    private float screenProp = 0f;

    private Bitmap captureBitmap;   //捕获的图片
    private Bitmap firstFrame;      //第一帧图片
    private String videoUrl;        //视频URL


    //切换摄像头按钮的参数
    private int iconSize = 0;       //图标大小
    private int iconMargin = 0;     //右上边距
    private int iconSrc = 0;        //图标资源
    private int iconLeft = 0;       //左图标
    private int iconRight = 0;      //右图标
    private int duration = 0;       //录制时间

    //缩放梯度
    private int zoomGradient = 0;

    private boolean firstTouch = true;
    private float firstTouchLength = 0;
    private JCameraPreViewListener jCameraPreViewListener;
    private Context context;
    private TextView tv_tip;
    private TextView tv_confirm;
    private ImageView iv_back;
    private ImageView iv_takePhoto;
    private List<Bitmap> bitmapList;
    private List<Boolean> isVerticalList;
    private RecyclerView rvImages;
    private RecyclerviewAdapter imagesAdapter;
    private int maxNum;//可拍摄最大数量
    private int width;
    private int bitmapMaxSize = 200;//bitmap压缩 尺寸 kb
    private float height;
    private MultiSelectConfig selectConfig;

    public JCameraView(Context context) {
        this(context, null);
    }

    public JCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;


        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        width = dm.widthPixels;
        height = (float) 4 / 3 * width;


        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.JCameraView, defStyleAttr, 0);
//        iconSize = a.getDimensionPixelSize(R.styleable.JCameraView_easy_iconSize, (int) TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_SP, 35, getResources().getDisplayMetrics()));
        iconMargin = a.getDimensionPixelSize(R.styleable.JCameraView_easy_iconMargin, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics()));
        iconSrc = a.getResourceId(R.styleable.JCameraView_easy_iconSrc, R.drawable.ic_camera_view_camera_easy_photos);
        iconLeft = a.getResourceId(R.styleable.JCameraView_easy_iconLeft, 0);
        iconRight = a.getResourceId(R.styleable.JCameraView_easy_iconRight, 0);
        duration = a.getInteger(R.styleable.JCameraView_easy_duration_max, 15 * 1000);       //没设置默认为15s
        a.recycle();
        initData();
        initView();
        initPreviewList();
    }


    private void initData() {
        bitmapList = new ArrayList<>();
        isVerticalList = new ArrayList<>();
        layout_width = ScreenUtils.getScreenWidth(getContext());
        //缩放梯度
        zoomGradient = (int) (layout_width / 16f);
        LogUtil.i("zoom = " + zoomGradient);
        machine = new CameraMachine(getContext(), this, this);
    }

//    public void setDuration(int duration) {
//        mCaptureLayout.setDuration(duration);
//    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        setWillNotDraw(false);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.camera_view, this);
        mVideoView = (VideoView) view.findViewById(R.id.video_preview);
        mPhoto = (ImageView) view.findViewById(R.id.image_photo);
        mVideoView.setLayoutParams(new LayoutParams(width, (int) height));
        mPhoto.setLayoutParams(new LayoutParams(width, (int) height));
        mSwitchCamera = (ImageView) view.findViewById(R.id.image_switch);
        mSwitchCamera.setImageResource(iconSrc);
        mFlashLamp = (ImageView) view.findViewById(R.id.image_flash);
        setFlashRes();
        mFlashLamp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                type_flash++;
                if (type_flash > 0x023) {
                    type_flash = TYPE_FLASH_AUTO;
                }
                setFlashRes();
            }
        });
        iv_back = (ImageView) view.findViewById(R.id.iv_back);
        iv_takePhoto = (ImageView) view.findViewById(R.id.iv_takePhoto);
        tv_tip = (TextView) view.findViewById(R.id.tv_tip);
        tv_confirm = (TextView) view.findViewById(R.id.tv_confirm);
        rvImages = findViewById(R.id.rv_images);

        mFoucsView = (FoucsView) view.findViewById(R.id.fouce_view);
        mVideoView.getHolder().addCallback(this);
        //切换摄像头
        mSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                machine.swtich(mVideoView.getHolder(), screenProp);
            }
        });

        iv_takePhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhoto.getVisibility() == VISIBLE) {
                    mPhoto.setVisibility(INVISIBLE);
                    tv_tip.setText("拍照");
                    return;
                }
                if (bitmapList.size() >= maxNum) {
                    ToastUtils.showToastError(context, "最多只能拍(" + maxNum + ")张!");
                    return;
                }
                mFoucsView.setEnabled(true);
                mSwitchCamera.setVisibility(INVISIBLE);
                mFlashLamp.setVisibility(INVISIBLE);
                machine.capture();
                iv_takePhoto.setEnabled(false);
            }
        });
        iv_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhoto.setVisibility(INVISIBLE);
            }
        });
        tv_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                jCameraLisenter.captureSuccess(bitmapList);
            }
        });
        mVideoView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if (event.getPointerCount() == 1) {
                            //显示对焦指示器
                            setFocusViewWidthAnimation(event.getX(), event.getY());
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() == 1) {
                            firstTouch = true;
                        }
                        if (event.getPointerCount() == 2) {
                            //第一个点
                            float point_1_X = event.getX(0);
                            float point_1_Y = event.getY(0);
                            //第二个点
                            float point_2_X = event.getX(1);
                            float point_2_Y = event.getY(1);

                            float result = (float) Math.sqrt(Math.pow(point_1_X - point_2_X, 2) + Math.pow(point_1_Y - point_2_Y, 2));

                            if (firstTouch) {
                                firstTouchLength = result;
                                firstTouch = false;
                            } else {
                                if ((int) (result - firstTouchLength) / zoomGradient != 0) {
                                    int zoom = (int) ((result - firstTouchLength) / 12);
                                    if (CameraInterface.isRecorder) {
                                        int realZoom = CameraInterface.getInstance().getCameraScaleRate() + zoom;
                                        machine.zoom(realZoom, CameraInterface.TYPE_RECORDER);
                                    } else {
                                        machine.zoom(zoom, CameraInterface.TYPE_CAPTURE);
                                    }
                                    firstTouch = true;
                                    Log.i("CJT", "result = " + (result - firstTouchLength));
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        firstTouch = true;
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = mVideoView.getMeasuredWidth();
        float heightSize = mVideoView.getMeasuredHeight();
        if (screenProp == 0) {
            screenProp = heightSize / widthSize;
        }
    }

    @Override
    public void cameraHasOpened() {
        CameraInterface.getInstance().doStartPreview(mVideoView.getHolder(), screenProp);
        //  setSuitableParams();
    }

//    private void setSuitableParams() {
//        if (screenProp > 1.8) { //屏幕的宽高比大于1.8的时候处理
//            float previewProp = CameraInterface.getInstance().getPreviewProp();
//            if (previewProp == 0 || mVideoView == null) {   //获取的size宽高比
//                return;
//            }
//            int measuredHeight = mVideoView.getMeasuredHeight();
//            int measuredWidth = mVideoView.getMeasuredWidth();
//            float clacWidth = measuredHeight / previewProp;   //计算出要显示的预览界面的宽度。
//            ViewGroup.LayoutParams layoutParams = mVideoView.getLayoutParams();
//            if (layoutParams == null) {
//                layoutParams = new ViewGroup.LayoutParams((int) clacWidth, measuredHeight);
//            }
//            if (clacWidth > 800 && Math.abs(clacWidth - measuredWidth) > clacWidth * 0.1F) {  //计算的宽度大于 800 并且和显示正常的布局的误差超过10%
//                layoutParams.width = (int) clacWidth;
//            }
//            final ViewGroup.LayoutParams finalLayoutParams = layoutParams;
//            mVideoView.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (mVideoView != null) {
//                        mVideoView.setLayoutParams(finalLayoutParams);
//                    }
//                }
//            });
//        }
//    }


    //生命周期onResume
    public void onResume() {
        LogUtil.i("JCameraView onResume");
        resetState(TYPE_DEFAULT); //重置状态
        CameraInterface.getInstance().registerSensorManager(getContext());
        CameraInterface.getInstance().setSwitchView(mSwitchCamera, mFlashLamp);
        machine.start(mVideoView.getHolder(), screenProp);
    }

    //生命周期onPause
    public void onPause() {
        LogUtil.i("JCameraView onPause");
        stopVideo();
        resetState(TYPE_PICTURE);
        CameraInterface.getInstance().isPreview(false);
        CameraInterface.getInstance().unregisterSensorManager(getContext());
    }

    //SurfaceView生命周期
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtil.i("JCameraView SurfaceCreated");
        new Thread() {
            @Override
            public void run() {
                CameraInterface.getInstance().doOpenCamera(JCameraView.this);
            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.i("JCameraView SurfaceDestroyed");
        CameraInterface.getInstance().doDestroyCamera();
    }

    //对焦框指示器动画
    private void setFocusViewWidthAnimation(float x, float y) {
        machine.foucs(x, y, new CameraInterface.FocusCallback() {
            @Override
            public void focusSuccess() {
                mFoucsView.setVisibility(INVISIBLE);
            }
        });
    }

    private void updateVideoViewSize(float videoWidth, float videoHeight) {
        if (videoWidth > videoHeight) {
            LayoutParams videoViewParam;
            int height = (int) ((videoHeight / videoWidth) * getWidth());
            videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, height);
            videoViewParam.gravity = Gravity.CENTER;
            mVideoView.setLayoutParams(videoViewParam);
        }
    }

    /**************************************************
     * 对外提供的API*
     **************************************************/

    public void setSaveVideoPath(String path) {
        CameraInterface.getInstance().setSaveVideoPath(path);
    }


    public void setJCameraListener(JCameraListener jCameraListener) {
        this.jCameraLisenter = jCameraListener;
    }


    public void setPreViewListener(JCameraPreViewListener jCameraPreViewListener) {
        this.jCameraPreViewListener = jCameraPreViewListener;
    }


    private ErrorListener errorListener;

    //启动Camera错误回调
    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
        CameraInterface.getInstance().setErrorListener(errorListener);
    }

    //设置CaptureButton功能（拍照和录像）
    public void setFeatures(int state) {
        // this.mCaptureLayout.setButtonFeatures(state);
    }

    //设置录制质量
    public void setMediaQuality(int quality) {
        CameraInterface.getInstance().setMediaQuality(quality);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void resetState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                //初始化VideoView
                FileUtil.deleteFile(videoUrl);
                mVideoView.setLayoutParams(new LayoutParams(width, (int) height));
                machine.start(mVideoView.getHolder(), screenProp);
                if (jCameraPreViewListener != null) {
                    jCameraPreViewListener.stop(TYPE_VIDEO);
                }
                break;
            case TYPE_PICTURE:
                mPhoto.setVisibility(INVISIBLE);
                if (jCameraPreViewListener != null) {
                    jCameraPreViewListener.stop(TYPE_PICTURE);
                }
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                mVideoView.setLayoutParams(new LayoutParams(width, (int) height));
                break;
        }
        mSwitchCamera.setVisibility(VISIBLE);
        mFlashLamp.setVisibility(VISIBLE);
        //mCaptureLayout.resetCaptureLayout();
    }

    @Override
    public void confirmState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                mVideoView.setLayoutParams(new LayoutParams(width, (int) height));
                machine.start(mVideoView.getHolder(), screenProp);
//                if (jCameraLisenter != null) {
//                    jCameraLisenter.recordSuccess(videoUrl, firstFrame);
//                }
                break;
            case TYPE_PICTURE:
                mPhoto.setVisibility(INVISIBLE);
//                if (jCameraLisenter != null) {
//                    jCameraLisenter.captureSuccess(captureBitmap);
//                }
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                break;
        }
        //  mCaptureLayout.resetCaptureLayout();
    }

    //打印bitmap信息
    private void logBitmapInfo(Bitmap bitmap) {
        Log.i("logBitmapInfo-----", "width: " + bitmap.getWidth() + "  height : " + bitmap.getHeight());
        float bitmapSize = ((float) bitmap.getAllocationByteCount()) / 1024 / 1024;
        Log.i("MlogBitmapInfo-----", "内存大小  " + bitmapSize);
    }

    @Override
    public void showPicture(Bitmap bitmap, boolean isVertical) {

//        captureBitmap = BitmapUtils.imageZoom(bitmap,200*1024);
        logBitmapInfo(bitmap);
        iv_takePhoto.setEnabled(true);
        isVerticalList.add(isVertical);
        if (selectConfig.isCompress) {
            bitmapList.add(BitmapUtils.imageZoom(bitmap, bitmapMaxSize));
        }
        bitmapList.add(bitmap);
        tv_confirm.setText("确认(" + bitmapList.size() + ")");
        imagesAdapter.setListData(bitmapList);
        resetState(TYPE_DEFAULT); //重置状态
        machine.cancel(mVideoView.getHolder(), screenProp);

    }

    private void initPreviewList() {
        rvImages.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        imagesAdapter = new RecyclerviewAdapter(context, bitmapList);
        rvImages.setAdapter(imagesAdapter);

    }

    public void resetBitmap(List<Bitmap> bitmaps) {
        bitmapList.clear();
        bitmapList.addAll(bitmaps);
        imagesAdapter.setListData(bitmapList);
    }

    public void setSelectConfig(MultiSelectConfig config) {
        selectConfig = config;
        bitmapMaxSize = selectConfig.maxSize;
    }

    public class RecyclerviewAdapter extends RecyclerView.Adapter<RecyclerviewAdapter.ViewHolder> {

        private Context context;
        private List<Bitmap> data;

        public RecyclerviewAdapter(Context context, List<Bitmap> data) {
            this.context = context;
            this.data = data;

        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_takephoto_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            holder.iv_image.setImageBitmap(data.get(position));
            holder.iv_image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mVideoView.setLayoutParams(new LayoutParams(width, (int) height));
                    if (isVerticalList.get(position)) {
                        mPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
                    } else {
                        mPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                    mPhoto.setImageBitmap(data.get(position));
                    mPhoto.setVisibility(VISIBLE);
                    mFoucsView.setEnabled(false);
                    tv_tip.setText("点击拍继续拍照");
                }
            });
            holder.iv_del.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    isVerticalList.remove(position);
                    //  data.remove(position);
                    bitmapList.remove(position);
                    tv_confirm.setText("确认(" + bitmapList.size() + ")");
                    mPhoto.setVisibility(INVISIBLE);
                    notifyDataSetChanged();
                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setListData(List<Bitmap> listData) {
            this.data = listData;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView iv_image;
            private ImageView iv_del;

            public ViewHolder(View itemView) {
                super(itemView);
                iv_image = itemView.findViewById(R.id.iv_image);
                iv_del = itemView.findViewById(R.id.iv_del);

            }
        }
    }

    @Override
    public void playVideo(Bitmap firstFrame, final String url) {
        videoUrl = url;
        JCameraView.this.firstFrame = firstFrame;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                try {
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                    } else {
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer.setDataSource(url);
                    mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                            .OnVideoSizeChangedListener() {
                        @Override
                        public void
                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
                                    .getVideoHeight());
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                        }
                    });
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        if (jCameraPreViewListener != null) {
            jCameraPreViewListener.start(TYPE_VIDEO);
        }
    }

    @Override
    public void stopVideo() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void setTip(String tip) {
        //mCaptureLayout.setTip(tip);
    }

    @Override
    public void enableCameraTip(boolean enable) {
        // mCaptureLayout.enableCameraTip(enable);
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    @Override
    public void startPreviewCallback() {
        LogUtil.i("startPreviewCallback");
        handlerFoucs(mFoucsView.getWidth() / 2, mFoucsView.getHeight() / 2);
    }

    @Override
    public boolean handlerFoucs(float x, float y) {
//        if (y > mCaptureLayout.getTop()) {
//            return false;
//        }
        mFoucsView.setVisibility(VISIBLE);
        if (x < mFoucsView.getWidth() / 2) {
            x = mFoucsView.getWidth() / 2;
        }
        if (x > layout_width - mFoucsView.getWidth() / 2) {
            x = layout_width - mFoucsView.getWidth() / 2;
        }
        if (y < mFoucsView.getWidth() / 2) {
            y = mFoucsView.getWidth() / 2;
        }
//        if (y > mCaptureLayout.getTop() - mFoucsView.getWidth() / 2) {
//            y = mCaptureLayout.getTop() - mFoucsView.getWidth() / 2;
//        }
        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
        return true;
    }

    public void setLeftClickListener(ClickListener clickListener) {
        this.leftClickListener = clickListener;
    }

    public void setRightClickListener(ClickListener clickListener) {
        this.rightClickListener = clickListener;
    }

    private void setFlashRes() {
        switch (type_flash) {
            case TYPE_FLASH_AUTO:
                mFlashLamp.setImageResource(R.drawable.ic_camera_view_flash_auto);
                machine.flash(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            case TYPE_FLASH_ON:
                mFlashLamp.setImageResource(R.drawable.ic_camera_view_flash_on);
                machine.flash(Camera.Parameters.FLASH_MODE_ON);
                break;
            case TYPE_FLASH_OFF:
                mFlashLamp.setImageResource(R.drawable.ic_camera_view_flash_off);
                machine.flash(Camera.Parameters.FLASH_MODE_OFF);
                break;
        }
    }
}
