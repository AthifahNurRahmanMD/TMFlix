package com.example.tmflix.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmflix.R;
import com.example.tmflix.model.ModelTv;
import com.example.tmflix.networking.ApiConfig;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class TvAdapter extends RecyclerView.Adapter<TvAdapter.ViewHolder> {

    private List<ModelTv> items;
    private TvAdapter.onSelectData onSelectData;
    private Context mContext;
    private double rating;

    public interface onSelectData {
        void onSelected(ModelTv modelTv);
    }

    public TvAdapter(Context context, List<ModelTv> items, TvAdapter.onSelectData xSelectData) {
        this.mContext = context;
        this.items = items;
        this.onSelectData = xSelectData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_film, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ModelTv data = items.get(position);

        rating = data.getVoteAverage();
        holder.tvTitle.setText(data.getName());
        holder.tvReleaseDate.setText(data.getReleaseDate());
        holder.tvDesc.setText(data.getOverview());

        float newValue = (float) rating;
        holder.ratingBar.setNumStars(5);
        holder.ratingBar.setStepSize(0.5f);
        holder.ratingBar.setRating(newValue / 2);

        Glide.with(mContext)
                .load(ApiConfig.URL_IMAGE + data.getPosterPath())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_image)
                        .transform(new RoundedCorners(16)))
                .into(holder.imgPhoto);

        holder.cvFilm.setOnClickListener(v -> onSelectData.onSelected(data));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView cvFilm;
        public ImageView imgPhoto;
        public TextView tvTitle;
        public TextView tvReleaseDate;
        public TextView tvDesc;
        public RatingBar ratingBar;

        public ViewHolder(View itemView) {
            super(itemView);
            cvFilm = itemView.findViewById(R.id.cvFilm);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvReleaseDate = itemView.findViewById(R.id.tvReleaseDate);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
