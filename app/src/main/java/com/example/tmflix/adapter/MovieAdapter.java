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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.tmflix.R;
import com.example.tmflix.model.ModelMovie;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private List<ModelMovie> items;
    private onSelectData onSelectData;
    private Context mContext;

    public interface onSelectData {
        void onSelected(ModelMovie modelMovie);
    }

    public MovieAdapter(Context context, List<ModelMovie> items, onSelectData onSelectData) {
        this.mContext = context;
        this.items = items;
        this.onSelectData = onSelectData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_film, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ModelMovie data = items.get(position);

        holder.tvTitle.setText(data.getTitle());
        holder.tvReleaseDate.setText(data.getReleaseDate());
        holder.tvDesc.setText(data.getOverview());

        float ratingValue = (float) data.getVoteAverage();
        holder.ratingBar.setNumStars(5);
        holder.ratingBar.setStepSize(0.5f);
        holder.ratingBar.setRating(ratingValue / 2); // Karena rating dari 10, bagi 2 jadi 5 stars

        Glide.with(mContext)
                .load("https://image.tmdb.org/t/p/w185/" + data.getPosterPath())
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


    // ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cvFilm;
        ImageView imgPhoto;
        TextView tvTitle;
        TextView tvReleaseDate;
        TextView tvDesc;
        RatingBar ratingBar;

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
