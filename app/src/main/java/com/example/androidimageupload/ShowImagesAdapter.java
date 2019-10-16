package com.example.androidimageupload;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

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
        final UploadModel uploadModel = uploadModels.get(position);
        holder.imageName.setText(uploadModel.getmName());

        Glide.with(mContext)
                .load(uploadModel.getmImageUrl()).thumbnail(0.1f)  // for showing the thumbnail
                .into(holder.image);

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openZoomDialog(uploadModel.getmImageUrl());
            }
        });

    }


    // dialog is opened on clicking the ImageView

    private void openZoomDialog(String url) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_zoom_in);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        PhotoView photoView = dialog.findViewById(R.id.zoon_in_view);
        Glide.with(mContext)
                .load(url)
                .into(photoView);

        dialog.show();
        ImageView closeView = dialog.findViewById(R.id.close_image_view);
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

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
