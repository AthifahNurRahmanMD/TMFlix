package com.example.tmflix.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tmflix.R;
import com.example.tmflix.model.ModelTrailer;
import com.example.tmflix.model.ModelTv;
import com.example.tmflix.model.TrailerResponse;
import com.example.tmflix.networking.ApiConfig;
import com.example.tmflix.networking.ApiService;
import com.example.tmflix.networking.RetrofitClientInstance;
import com.example.tmflix.sqlite.DatabaseHelper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailTelevisionActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView tvTitle, tvName, tvRating, tvRelease, tvPopularity, tvOverview;
    ImageView imgCover, imgPhoto;
    ImageButton imgFavorite;
    RatingBar ratingBar;
    RecyclerView rvTrailer;

    String NameFilm, ReleaseDate, Popularity, Overview, Cover, Thumbnail, movieURL;
    int Id;
    double Rating;
    ModelTv modelTV;
    ProgressDialog progressDialog;
    List<ModelTrailer> modelTrailer = new ArrayList<>();
    TrailerAdapter trailerAdapter;
    DatabaseHelper helper;
    boolean isFavorite = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Mohon Tunggu");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sedang menampilkan trailer");

        ratingBar = findViewById(R.id.ratingBar);
        imgCover = findViewById(R.id.imgCover);
        imgPhoto = findViewById(R.id.imgPhoto);
        imgFavorite = findViewById(R.id.imgFavorite);
        tvTitle = findViewById(R.id.tvTitle);
        tvName = findViewById(R.id.tvName);
        tvRating = findViewById(R.id.tvRating);
        tvRelease = findViewById(R.id.tvRelease);
        tvPopularity = findViewById(R.id.tvPopularity);
        tvOverview = findViewById(R.id.tvOverview);
        rvTrailer = findViewById(R.id.rvTrailer);

        helper = new DatabaseHelper(this);

        modelTV = (ModelTv) getIntent().getSerializableExtra("detailTV");
        if (modelTV != null) {
            Id = modelTV.getId();
            NameFilm = modelTV.getName();
            Rating = modelTV.getVoteAverage();
            ReleaseDate = modelTV.getReleaseDate();
            Popularity = modelTV.getPopularity();
            Overview = modelTV.getOverview();
            Cover = modelTV.getBackdropPath();
            Thumbnail = modelTV.getPosterPath();
            movieURL = ApiConfig.URL_FILM + Id;

            tvTitle.setText(NameFilm);
            tvName.setText(NameFilm);
            tvRating.setText(Rating + "/10");
            tvRelease.setText(ReleaseDate);
            tvPopularity.setText(Popularity);
            tvOverview.setText(Overview);
            tvTitle.setSelected(true);
            tvName.setSelected(true);

            float newValue = (float) Rating;
            ratingBar.setNumStars(5);
            ratingBar.setStepSize(0.5f);
            ratingBar.setRating(newValue / 2);

            // Cek apakah film ini sudah difavoritkan
            isFavorite = helper.isTvFavorited(Id);
            imgFavorite.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);


            Glide.with(this)
                    .load(ApiConfig.URL_IMAGE + Cover)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgCover);

            Glide.with(this)
                    .load(ApiConfig.URL_IMAGE + Thumbnail)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgPhoto);

            rvTrailer.setHasFixedSize(true);
            rvTrailer.setLayoutManager(new LinearLayoutManager(this));

            getTrailer();
        }

        imgFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                helper.deleteFavoriteTv(Id);
                Toast.makeText(this, NameFilm + " dihapus dari favorit", Toast.LENGTH_SHORT).show();
                imgFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            } else {
                helper.insertFavoriteTv(modelTV);
                Toast.makeText(this, NameFilm + " ditambahkan ke favorit", Toast.LENGTH_SHORT).show();
                imgFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            }
            isFavorite = !isFavorite;
        });
    }

    private void getTrailer() {
        progressDialog.show();
        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<TrailerResponse> call = apiService.getTVTrailers(
                Id,
                ApiConfig.API_KEY,
                ApiConfig.LANGUAGE);
        call.enqueue(new Callback<TrailerResponse>() {
            @Override
            public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_RESPONSE", "Data detail television: " + new Gson().toJson(response.body()));
                    modelTrailer = response.body().getResults();
                    showTrailer();
                } else {
                    Toast.makeText(DetailTelevisionActivity.this, "Gagal menampilkan trailer", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TrailerResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(DetailTelevisionActivity.this, "Koneksi internet gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTrailer() {
        trailerAdapter = new TrailerAdapter(this, modelTrailer);
        rvTrailer.setAdapter(trailerAdapter);
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams winParams = window.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        window.setAttributes(winParams);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
