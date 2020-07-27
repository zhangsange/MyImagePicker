package com.ypx.imagepicker.editLibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ypx.imagepicker.ImagePicker;
import com.ypx.imagepicker.bean.ImageItem;
import com.ypx.imagepicker.helper.recyclerviewitemhelper.ItemTouchHelperAdapter;
import com.ypx.imagepicker.widget.ShowTypeImageView;

import java.util.Collections;
import java.util.List;

/**
 * Time: 2019/7/23 10:43
 * Author:ypx
 * Description: 图片脱敏 下方图片预览Adapter
 */
public class MultiPreviewEditAdapter extends RecyclerView.Adapter<MultiPreviewEditAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private List<Bitmap> previewList;
    private Context context;
    private ImageItem previewImageItem;
    private int lastSelectPos;

    public void setPreviewImageItem(ImageItem previewImageItem) {
        this.previewImageItem = previewImageItem;
        notifyDataSetChanged();
    }

    public MultiPreviewEditAdapter(List<Bitmap> previewList) {
        this.lastSelectPos = 0;
        this.previewList = previewList;
    }
    public void  setListData(List<Bitmap> previewList){
        this.lastSelectPos = 0;
        this.previewList = previewList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ShowTypeImageView imageView = new ShowTypeImageView(context);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(dp(60), dp(60));
        params.leftMargin = dp(8);
        params.rightMargin = dp(8);
        params.topMargin = dp(15);
        params.bottomMargin = dp(15);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ViewHolder(imageView);
    }

    public void setSelectPos(int selectPos) {
        lastSelectPos = selectPos;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Bitmap bitmap = previewList.get(position);
        if (lastSelectPos == position) {
            holder.imageView.setSelect(true, ImagePicker.getThemeColor());
        } else {
            holder.imageView.setSelect(false, ImagePicker.getThemeColor());
        }

        Glide.with(context).load(bitmap).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MyIMGEditActivity) context).setmImgView(lastSelectPos, position);
                lastSelectPos = position;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return previewList.size();
    }

    public int dp(float dp) {
        if (context == null) {
            return 0;
        }
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        try {
            if (null == previewList
                    || fromPosition >= previewList.size()
                    || toPosition >= previewList.size()) {
                return true;
            }
            Collections.swap(previewList, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ShowTypeImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = (ShowTypeImageView) itemView;
        }
    }
}
