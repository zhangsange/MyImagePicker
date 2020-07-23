package com.ypx.imagepicker.editLibrary.core.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.ypx.imagepicker.editLibrary.core.util.BitmapUtil;


/**
 * Created by felix on 2017/12/26 下午3:07.
 */

public class IMGFileDecoder extends IMGDecoder {
    private Context mContext;

    public IMGFileDecoder(Context context, Uri uri) {
        super(uri);
        mContext = context;
    }

    @Override
    public Bitmap decode(BitmapFactory.Options options) {
        Uri uri = getUri();
        if (uri == null) {
            return null;
        }

        String path = uri.getPath();
        if (TextUtils.isEmpty(path)) {
            return null;
        }

/**
 * 处理android  10  不能创建bitmap问题
 */
        return BitmapUtil.getBitmapFromUri(mContext, BitmapUtil.getImageContentUri(mContext, path));
//        File file = new File(path);
//        if (file.exists()) {
//            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
//        }


    }
}
