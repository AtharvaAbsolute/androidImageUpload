package com.example.androidimageupload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ShowImagesAdapter extends RecyclerView.Adapter<ShowImagesAdapter.ImageViewHolder> {

    private Context mContext;
    private List<UploadModel> uploadModels;

    public ShowImagesAdapter(Context context, List<UploadModel> uploadModelList) {
        mContext = context;
        uploadModels = uploadModelList;

    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_image_item, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        UploadModel uploadModel = uploadModels.get(position);
        holder.imageName.setText(uploadModel.getmName());
        Glide.with(mContext)
                .load(uploadModel.getmImageUrl()).placeholder(mContext.getDrawable(R.drawable.gallery_click))
                .into(holder.image);

    }

    @Override
    public int getItemCount() {
        return uploadModels.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public TextView imageName;
        public ImageView image;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imageName = itemView.findViewById(R.id.image_name);
            image = itemView.findViewById(R.id.image_view);
        }
    }
}
