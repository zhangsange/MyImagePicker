package com.ypx.imagepicker.bean.selectconfig;

import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.bean.SelectMode;
import com.ypx.imagepicker.helper.ToastHelper;

import java.util.ArrayList;

/**
 * Description: 多选配置项
 * <p>
 * Author: peixing.yang
 * Date: 2019/2/21
 */
public class MultiSelectConfig extends CropConfig {
    private boolean isShowOriginalCheckBox;
    private boolean isDefaultOriginal;
    public boolean isCanEditPic;
    public boolean isCanPreviewVideo = true;
    public boolean isPreview = true;
    public boolean isDeleteOriginalPic = true;//是否删除原图
    public boolean isDeleteBeforeEditlPic = true;//是否删除编辑前(非原图(曾被编辑过的图片))的图片
    public boolean isSingleTakePhoto = false;//是否为拍照后直接脱敏

    public String imageSavePath = "";//脱敏图片保存路径

    public ToastHelper toastHelper;
    public void setToastHelper(ToastHelper toastHelper){
        this.toastHelper = toastHelper;
    }

    public boolean isCompress() {
        return isCompress;
    }

    public void setCompress(boolean compress) {
        isCompress = compress;
    }

    public boolean isCompress= false;//是否压缩

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int maxSize  = 400;//bitmap压缩 大小 kb

    public String getWaterMark() {
        return waterMark;
    }

    public void setWaterMark(String waterMark) {
        this.waterMark = waterMark;
    }

    public float getWaterMarkTextSize() {
        return waterMarkTextSize;
    }

    public void setWaterMarkTextSize(float waterMarkTextSize) {
        this.waterMarkTextSize = waterMarkTextSize;
    }

    public String getWaterMarkColor() {
        return waterMarkColor;
    }

    public void setWaterMarkColor(String waterMarkColor) {
        this.waterMarkColor = waterMarkColor;
    }

    public String waterMark ="";//是水印

    public float waterMarkTextSize=12f;//水印文字默认大小
    public String waterMarkColor ="#80FF0000";//是水印颜色

    public void setImgMaxNum(int imgMaxNum) {
        this.imgMaxNum = imgMaxNum;
    }

    public int getImgMaxNum() {
        return imgMaxNum;
    }

    public int imgMaxNum =50;//添加图片最大数量


    public boolean isDeleteOriginalPic() {
        return isDeleteOriginalPic;
    }

    public void setDeleteOriginalPic(boolean deleteOriginalPic) {
        isDeleteOriginalPic = deleteOriginalPic;
    }

    public boolean isDeleteBeforeEditlPic() {
        return isDeleteBeforeEditlPic;
    }

    public void setDeleteBeforeEditlPic(boolean deleteBeforeEditlPic) {
        isDeleteBeforeEditlPic = deleteBeforeEditlPic;
    }

    public boolean isSingleTakePhoto() {
        return isSingleTakePhoto;
    }

    public void setSingleTakePhoto(boolean singleTakePhoto) {
        isSingleTakePhoto = singleTakePhoto;
    }


    public String getImageSavePath() {
        return imageSavePath;
    }

    public void setImageSavePath(String imageSavePath) {
        this.imageSavePath = imageSavePath;
    }

//    private boolean isSeePreview = false;//仅仅为查看预览
//
//    public boolean isSeePreview() {
//        return isSeePreview;
//    }
//
//    public void setSeePreview(boolean seePreview) {
//        isSeePreview = seePreview;
//    }
//

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    private String number = "";


    public String getNumberColor() {
        return numberColor;
    }

    public void setNumberColor(String numberColor) {
        this.numberColor = numberColor;
    }

    private String numberColor = "";

    private int selectMode = SelectMode.MODE_MULTI;
    private ArrayList<ImageItem> lastImageList = new ArrayList<>();

    public boolean isPreview() {
        return isPreview;
    }

    public void setPreview(boolean preview) {
        isPreview = preview;
    }


    public ArrayList<ImageItem> getLastImageList() {
        return lastImageList;
    }

    public void setLastImageList(ArrayList<ImageItem> lastImageList) {
        this.lastImageList = lastImageList;
    }

    public int getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(int selectMode) {
        this.selectMode = selectMode;
    }

    public boolean isShowOriginalCheckBox() {
        return isShowOriginalCheckBox;
    }

    public void setShowOriginalCheckBox(boolean showOriginalCheckBox) {
        isShowOriginalCheckBox = showOriginalCheckBox;
    }

    public boolean isDefaultOriginal() {
        return isDefaultOriginal;
    }

    public void setDefaultOriginal(boolean defaultOriginal) {
        isDefaultOriginal = defaultOriginal;
    }

    public boolean isCanEditPic() {
        return isCanEditPic;
    }

    public void setCanEditPic(boolean canEditPic) {
        isCanEditPic = canEditPic;
    }

    /**
     * 是否是之前选中过的
     */
    public boolean isLastItem(ImageItem imageItem) {
        if (lastImageList == null || lastImageList.size() == 0) {
            return false;
        }
        return lastImageList.contains(imageItem);
    }

    public boolean isCanPreviewVideo() {
        return isCanPreviewVideo;
    }

    public void setCanPreviewVideo(boolean canPreviewVideo) {
        isCanPreviewVideo = canPreviewVideo;
    }
}
