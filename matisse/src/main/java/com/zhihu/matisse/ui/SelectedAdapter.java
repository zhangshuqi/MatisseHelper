package com.zhihu.matisse.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.Item;

import java.util.ArrayList;

public class SelectedAdapter extends RecyclerView.Adapter<SelectedAdapter.MyViewHolder> {

    /**
     * Empty collection
     */
    public static final int COLLECTION_UNDEFINED = 0x00;
    /**
     * Collection only with images
     */
    public static final int COLLECTION_IMAGE = 0x01;
    /**
     * Collection only with videos
     */
    public static final int COLLECTION_VIDEO = 0x01 << 1;
    ArrayList<Item> mItems;
    Context context;
    public SelectedAdapter(ArrayList<Item> mItems) {
        this.mItems = mItems;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected,parent,false);
        /*if (stateCollectionType == COLLECTION_UNDEFINED) {
            if (mItems.get(1).isImage()) {
                stateCollectionType = COLLECTION_IMAGE;
            } else if (item.isVideo()) {
                stateCollectionType = COLLECTION_VIDEO;
            }
        } else if (stateCollectionType == COLLECTION_IMAGE) {
            if (item.isVideo()) {
                stateCollectionType = COLLECTION_MIXED;
            }
        } else if (stateCollectionType == COLLECTION_VIDEO) {
            if (item.isImage()) {
                stateCollectionType = COLLECTION_MIXED;
            }
        }*/
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(mItems.get(position).uri)
                .into(holder.ivIcon);
        /*if (mItems.get(position).isVideo()){
            holder.videoDuration.setVisibility(View.VISIBLE);
            holder.videoDuration.setText(DateUtils.formatElapsedTime(mItems.get(position).duration / 1000));
        }else{
            holder.videoDuration.setVisibility(View.GONE);
        }*/
        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.delete(mItems.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mItems != null && mItems.size() > 0){
            return mItems.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivIcon,ivDelete;
        private final TextView videoDuration;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            videoDuration = itemView.findViewById(R.id.video_duration);
        }
    }

    OnDeleteListener listener;
    public interface OnDeleteListener{
        void delete(Item mItems,int position);
    }

    public void setOnDeleteListener(OnDeleteListener listener){
        this.listener = listener;
    }
}
