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

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmflix.R;
import com.example.tmflix.activities.DetailTelevisionActivity;
import com.example.tmflix.adapter.TvAdapter;
import com.example.tmflix.adapter.TvHorizontalAdapter;
import com.example.tmflix.model.ModelTv;
import com.example.tmflix.model.TVResponse;
import com.example.tmflix.networking.ApiConfig;
import com.example.tmflix.networking.ApiService;
import com.example.tmflix.networking.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentTv extends Fragment implements
        TvHorizontalAdapter.onSelectData, TvAdapter.onSelectData {

    private RecyclerView rvNowPlaying, rvFilmRecommend;
    private TvHorizontalAdapter tvHorizontalAdapter;
    private TvAdapter tvAdapter;
    private ProgressDialog progressDialog;
    private SearchView searchFilm;
    private List<ModelTv> tvPlayNow = new ArrayList<>();
    private List<ModelTv> tvPopular = new ArrayList<>();
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private Button btnLoadMore;

    public FragmentTv() {}

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
                setSearchTv(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")) {
                    currentPage = 1;
                    isLastPage = false;
                    getPopularTV(currentPage);
                }
                return false;
            }
        });

        int searchPlateId = searchFilm.getContext().getResources()
                .getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchFilm.findViewById(searchPlateId);
        if (searchPlate != null) searchPlate.setBackgroundColor(Color.TRANSPARENT);

        rvNowPlaying = rootView.findViewById(R.id.rvNowPlaying);
        rvNowPlaying.setHasFixedSize(true);
        rvNowPlaying.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rvNowPlaying);

        rvFilmRecommend = rootView.findViewById(R.id.rvFilmRecommend);
        btnLoadMore = rootView.findViewById(R.id.btnLoadMore);
        rvFilmRecommend.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvFilmRecommend.setHasFixedSize(true);

        btnLoadMore.setOnClickListener(v -> {
            currentPage++;
            getPopularTV(currentPage); // Memanggil data TV Popular untuk halaman berikutnya
        });

        getTVHorizontal();
        currentPage = 1;
        isLastPage = false;
        getPopularTV(currentPage);

        ((View) searchFilm.getParent()).setOnClickListener(v -> {
            searchFilm.setIconified(false);
            searchFilm.requestFocus();
        });

        return rootView;
    }

    // Search TV (tanpa pagination)
    private void setSearchTv(String query) {
        progressDialog.show();
        ApiService apiService = RetrofitClientInstance
                .getRetrofitInstance()
                .create(ApiService.class);
        Call<TVResponse> call = apiService.searchTV(
                ApiConfig.API_KEY,
                ApiConfig.LANGUAGE,
                query);
        call.enqueue(new Callback<TVResponse>() {
            @Override
            public void onResponse(Call<TVResponse> call, Response<TVResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    tvPopular.clear();
                    tvPopular.addAll(response.body().getResults());
                    showFilmTV(true);
                    isLastPage = true;
                } else {
                    Toast.makeText(getActivity(), "Gagal menampilkan data!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<TVResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTVHorizontal() {
        progressDialog.show();
        ApiService apiService = RetrofitClientInstance
                .getRetrofitInstance()
                .create(ApiService.class);
        Call<TVResponse> call = apiService.getTVNowPlaying(
                ApiConfig.API_KEY,
                ApiConfig.LANGUAGE);
        call.enqueue(new Callback<TVResponse>() {
            @Override
            public void onResponse(Call<TVResponse> call, Response<TVResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    tvPlayNow.clear();
                    tvPlayNow.addAll(response.body().getResults());
                    showTVHorizontal();
                }
            }
            @Override
            public void onFailure(Call<TVResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Pagination utama untuk TV Popular
    private void getPopularTV(int page) {
        progressDialog.show();
        ApiService apiService = RetrofitClientInstance
                .getRetrofitInstance()
                .create(ApiService.class);
        Call<TVResponse> call = apiService.getTVPopular(
                ApiConfig.API_KEY,
                ApiConfig.LANGUAGE,
                page
        );
        call.enqueue(new Callback<TVResponse>() {
            @Override
            public void onResponse(Call<TVResponse> call, Response<TVResponse> response) {
                progressDialog.dismiss();
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<ModelTv> newTvs = response.body().getResults();
                    if (page == 1) {
                        tvPopular.clear();
                        showFilmTV(false);
                    }
                    if (page > 1 && tvAdapter != null) {
                        tvAdapter.addTvies(newTvs);
                    } else if (tvAdapter != null) {
                        tvPopular.addAll(newTvs);
                        tvAdapter.notifyDataSetChanged();
                    } else {
                        tvPopular.addAll(newTvs);
                        showFilmTV(false);
                    }
                    // Tampilkan tombol jika masih ada page berikutnya
                    if (page < response.body().getTotalPages() && !newTvs.isEmpty()) {
                        btnLoadMore.setVisibility(View.VISIBLE);
                        isLastPage = false;
                    } else {
                        btnLoadMore.setVisibility(View.GONE);
                        isLastPage = true;
                    }
                }
            }
            @Override
            public void onFailure(Call<TVResponse> call, Throwable t) {
                progressDialog.dismiss();
                isLoading = false;
                Toast.makeText(getActivity(), "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTVHorizontal() {
        tvHorizontalAdapter = new TvHorizontalAdapter(getActivity(), tvPlayNow, this);
        rvNowPlaying.setAdapter(tvHorizontalAdapter);
        tvHorizontalAdapter.notifyDataSetChanged();
    }

    // Adapter setup vertical, isSearch: jika true berarti hasil search (bukan pagination)
    private void showFilmTV(boolean isSearch) {
        tvAdapter = new TvAdapter(getActivity(), tvPopular, this);
        rvFilmRecommend.setAdapter(tvAdapter);
        if (isSearch) {
            tvAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSelected(ModelTv modelTV) {
        Intent intent = new Intent(getActivity(), DetailTelevisionActivity.class);
        intent.putExtra("detailTV", modelTV);
        startActivity(intent);
    }
}