package com.example.tmflix.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.tmflix.R;
import com.example.tmflix.model.ModelMovie;
import com.example.tmflix.networking.ApiConfig;

import java.util.List;

public class MovieHorizontalAdapter extends RecyclerView.Adapter<MovieHorizontalAdapter.ViewHolder> {

    private List<ModelMovie> items;
    private onSelectData onSelectData; // Deklarasi interface
    private Context mContext;

    public interface onSelectData {
        void onSelected(ModelMovie modelMovie);
    }

    // Konstruktor harus menerima onSelectData
    public MovieHorizontalAdapter(Context context, List<ModelMovie> items, onSelectData onSelectData) {
        this.mContext = context;
        this.items = items;
        this.onSelectData = onSelectData; // Inisialisasi
    }

    @Override
    public MovieHorizontalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_film_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieHorizontalAdapter.ViewHolder holder, int position) {
        final ModelMovie data = items.get(position);
        holder.tvTitle.setText(data.getTitle());

        Glide.with(mContext)
                .load(ApiConfig.URL_IMAGE + data.getBackdropPath())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_image)
                        .transform(new RoundedCorners(10)))
                .into(holder.imgPhoto);

        // Tambahkan listener klik di sini
        holder.imgPhoto.setOnClickListener(v -> {
            if (onSelectData != null) { // Pastikan interface tidak null
                onSelectData.onSelected(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;
        TextView tvTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
