package com.ypx.imagepicker.editLibrary.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/4/25
 * 描    述：
 * =====================================
 */
public class FileUtil {
    private static final File parentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    private static String storagePath = "";
    public static String PIC_EDIT_FOLDER_NAME = "imagePicker_Edit";
    public static String PIC_FOLDER_NAME = "imagePicker";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static String initPath(String dir) {
        if (storagePath.equals("")) {
            storagePath = parentPath.getAbsolutePath() + File.separator + dir;
            File f = new File(storagePath);
            if (!f.exists()) {
                f.mkdir();
            }
        }
        return storagePath;
    }

    public static String saveBitmap(String dir, Bitmap b, Context context) {
        String path = initPath(dir);
        long dataTake = System.currentTimeMillis();
        String jpegName = path + File.separator + "IMG_" + dataTake + ".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + jpegName)));
            return jpegName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 将本地图片转成Bitmap
     * @param path 已有图片的路径
     * @return
     */
    public static Bitmap getBitmap(String path){
            Bitmap bitmap = null;
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
                bitmap = BitmapFactory.decodeStream(bis);
                bis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;

    }


    public static String saveBitmapAndroidQ(Context context, String dir, Bitmap b) {
        long dataTake = System.currentTimeMillis();
        String jpegName = "IMG_" + dataTake + ".jpg";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, jpegName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/" + dir);

        Uri external;
        ContentResolver resolver = context.getContentResolver();
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            external = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        }

        Uri insertUri = resolver.insert(external, values);
        if (insertUri == null) {
            return "";
        }
        OutputStream os;
        try {
            os = resolver.openOutputStream(insertUri);
            b.compress(Bitmap.CompressFormat.JPEG, 100, os);
            if (os != null) {
                os.flush();
                os.close();
            }
            return getRealPathFromUri(context,insertUri);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //删除文件后更新数据库  通知媒体库更新文件夹,！！！！！filepath（文件夹路径）要求尽量精确，以防删错
    public static void deletePic(Context context, String filepath){

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = context.getContentResolver();
        String where = MediaStore.Images.Media.DATA + "='" + filepath + "'";
//删除图片
        int i=   mContentResolver.delete(uri, where, null);
        updateMediaStore(context,filepath);

    }
    public static void updateMediaStore(final  Context context, final String path) {
        //版本号的判断  4.4为分水岭，发送广播更新媒体库
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            MediaScannerConnection.scanFile(context, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaScanIntent.setData(uri);
                    context.sendBroadcast(mediaScanIntent);
                }
            });
        } else {
            File file = new File(path);
            String relationDir = file.getParent();
            File file1 = new File(relationDir);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file1.getAbsoluteFile())));
        }
    }

//    public static void deletePic(Context context,String path) {
//        File file = new File(path);
//        //删除系统缩略图
//        context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", new String[]{path});
//        //删除手机中图片
//        file.delete();
//    }
    public static String copy2DCIMAndroidQ(Context context, String path, String saveDirName) {
        String[] splits = path.split("/");
        String fileName = splits[splits.length - 1];

        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/" + saveDirName);

        Uri external;
        ContentResolver resolver = context.getContentResolver();
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            external = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            external = MediaStore.Video.Media.INTERNAL_CONTENT_URI;
        }
        Uri insertUri = resolver.insert(external, values);
        if (insertUri == null) {
            return "";
        }

        try {
            InputStream is = null;
            OutputStream os = resolver.openOutputStream(insertUri);
            if (os == null) {
                return "";
            }
            int read;
            File sourceFile = new File(path);
            if (sourceFile.exists()) { // 文件存在时
                is = new FileInputStream(sourceFile); // 读入原文件
                byte[] buffer = new byte[4096];
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
            if (is != null) is.close();
            os.close();
            return insertUri.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean deleteFile(String url) {
        boolean result = false;
        File file = new File(url);
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
