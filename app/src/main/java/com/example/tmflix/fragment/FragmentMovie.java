package com.example.tmflix.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.example.tmflix.R;
import com.example.tmflix.activities.DetailMovieActivity;
import com.example.tmflix.adapter.MovieAdapter;
import com.example.tmflix.adapter.MovieHorizontalAdapter;
import com.example.tmflix.model.ModelMovie;
import com.example.tmflix.model.MovieResponse;
import com.example.tmflix.networking.ApiConfig;
import com.example.tmflix.networking.ApiService;
import com.example.tmflix.networking.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentMovie extends Fragment
        implements MovieHorizontalAdapter.onSelectData, MovieAdapter.onSelectData {

    private RecyclerView rvNowPlaying, rvFilmRecommend;
    private MovieHorizontalAdapter movieHorizontalAdapter;
    private MovieAdapter movieAdapter;
    private ProgressDialog progressDialog;
    private SearchView searchFilm;
    private List<ModelMovie> moviePlayNow = new ArrayList<>();
    private List<ModelMovie> moviePopular = new ArrayList<>();
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private Button btnLoadMore;

    public FragmentMovie() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_film, container, false);

        // Inisialisasi ProgressDialog
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Mohon Tunggu");
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Sedang menampilkan data");

        // Inisialisasi SearchView
        searchFilm = rootView.findViewById(R.id.searchFilm);
        searchFilm.setQueryHint(getString(R.string.search_film));
        searchFilm.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setSearchMovie(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")) {
                    currentPage = 1;
                    isLastPage = false;
                    getMovie(currentPage);
                }
                return false;
            }
        });

        int searchPlateId = searchFilm.getContext()
                .getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchFilm.findViewById(searchPlateId);
        if (searchPlate != null) searchPlate.setBackgroundColor(Color.TRANSPARENT);

        // Now Playing (horizontal)
        rvNowPlaying = rootView.findViewById(R.id.rvNowPlaying);
        rvNowPlaying.setHasFixedSize(true);
        rvNowPlaying.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rvNowPlaying);

        // Rekomendasi Film (vertical)
        rvFilmRecommend = rootView.findViewById(R.id.rvFilmRecommend);
        btnLoadMore = rootView.findViewById(R.id.btnLoadMore);
        rvFilmRecommend.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvFilmRecommend.setHasFixedSize(true);

        // Set listener
        btnLoadMore.setOnClickListener(v -> {
            currentPage++;
            getMovie(currentPage);
        });

        // Load data awal
        getMovieHorizontal();
        currentPage = 1;
        isLastPage = false;
        getMovie(currentPage);

        ((View) searchFilm.getParent()).setOnClickListener(v -> {
            searchFilm.setIconified(false);
            searchFilm.requestFocus();
        });

        return rootView;
    }

    // Search Movie (tanpa pagination)
    private void setSearchMovie(String query) {
        progressDialog.show();
        ApiService apiService = RetrofitClientInstance
                .getRetrofitInstance()
                .create(ApiService.class);
        Call<MovieResponse> call = apiService.searchMovie(
                ApiConfig.API_KEY,
                ApiConfig.LANGUAGE,
                query);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    moviePopular.clear();
                    moviePopular.addAll(response.body().getResults());
                    showMovie(true);
                    isLastPage = true;
                } else {
                    Toast.makeText(getActivity(), "Gagal menampilkan data!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Now Playing horizontal (tidak perlu pagination)
    private void getMovieHorizontal() {
        progressDialog.show();
        ApiService apiService = RetrofitClientInstance
                .getRetrofitInstance()
                .create(ApiService.class);
        Call<MovieResponse> call = apiService.getNowPlaying(
                ApiConfig.API_KEY,
                ApiConfig.LANGUAGE);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    moviePlayNow.clear();
                    moviePlayNow.addAll(response.body().getResults());
                    showMovieHorizontal();
                }
            }
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Pagination utama
    private void getMovie(int page) {
        progressDialog.show();
        ApiService apiService = RetrofitClientInstance
                .getRetrofitInstance()
                .create(ApiService.class);
        Call<MovieResponse> call = apiService.getPopularMovies(
                ApiConfig.API_KEY,
                ApiConfig.LANGUAGE,
                page
        );
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                progressDialog.dismiss();
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<ModelMovie> newMovies = response.body().getResults();

                    // PAGE 1: refresh total list+adapter
                    if (page == 1) {
                        moviePopular.clear();
                        moviePopular.addAll(newMovies);
                        showMovie(false); // Adapter baru jika page 1
                    } else if (movieAdapter != null) {
                        // Tambah data & update adapter (pagination)
                        movieAdapter.addMovies(newMovies);
                    }

                    // Tampilkan tombol Load More jika masih ada page berikutnya
                    if (getView() != null) {
                        Button btnLoadMore = getView().findViewById(R.id.btnLoadMore);
                        if (page < response.body().getTotalPages() && !newMovies.isEmpty()) {
                            btnLoadMore.setVisibility(View.VISIBLE);
                            isLastPage = false;
                        } else {
                            btnLoadMore.setVisibility(View.GONE);
                            isLastPage = true;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                progressDialog.dismiss();
                isLoading = false;
                Toast.makeText(getActivity(), "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter setup horizontal
    private void showMovieHorizontal() {
        movieHorizontalAdapter = new MovieHorizontalAdapter(getActivity(), moviePlayNow, this);
        rvNowPlaying.setAdapter(movieHorizontalAdapter);
        movieHorizontalAdapter.notifyDataSetChanged();
    }

    // Adapter setup vertical, isSearch: jika true berarti hasil search (bukan pagination)
    private void showMovie(boolean isSearch) {
        movieAdapter = new MovieAdapter(getActivity(), moviePopular, this);
        rvFilmRecommend.setAdapter(movieAdapter);
        if (isSearch) {
            movieAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSelected(ModelMovie modelMovie) {
        Intent intent = new Intent(getActivity(), DetailMovieActivity.class);
        intent.putExtra("detailMovie", modelMovie);
        startActivity(intent);
    }
}