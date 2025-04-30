package com.example.memo_saic;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private static final String TAG = "PhotoAdapter";
    private final Context context;
    private final List<PhotoData> photoList;
    private final OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(PhotoData photo, int position);
    }

    public PhotoAdapter(Context context, List<PhotoData> photoList, OnPhotoClickListener listener) {
        this.context = context;
        this.photoList = photoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoData photo = photoList.get(position);

        Log.d(TAG, "Binding photo at position " + position + ": " + photo.getImageUrl());

        // Load image with Glide
        try {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.sample)
                    .error(R.drawable.sample)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

            Glide.with(context)
                    .load(photo.getImageUrl())
                    .apply(requestOptions)
                    .into(holder.photoImageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            holder.photoImageView.setImageResource(R.drawable.sample);
        }

        // Set caption/location text
        String displayText = photo.getDistrict();
        if (photo.getState() != null && !photo.getState().isEmpty()) {
            displayText += ", " + photo.getState();
        }
        holder.captionTextView.setText(displayText);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoClick(photo, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        TextView captionTextView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            captionTextView = itemView.findViewById(R.id.captionTextView);
        }
    }
}