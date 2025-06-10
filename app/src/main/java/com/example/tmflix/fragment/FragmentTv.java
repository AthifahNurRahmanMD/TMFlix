package com.example.tmflix.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmflix.R;
import com.example.tmflix.activities.DetailTelevisionActivity;
import com.example.tmflix.adapter.TvAdapter;
import com.example.tmflix.adapter.TvHorizontalAdapter;
import com.example.tmflix.model.Genre;
import com.example.tmflix.model.GenreListResponse;
import com.example.tmflix.model.ModelTv;
import com.example.tmflix.model.TVResponse;
import com.example.tmflix.networking.ApiConfig;
import com.example.tmflix.networking.ApiService;
import com.example.tmflix.networking.RetrofitClientInstance;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentTv extends Fragment implements
        TvHorizontalAdapter.onSelectData, TvAdapter.onSelectData {

    private RecyclerView rvNowPlaying, rvFilmRecommend;
    private TvHorizontalAdapter tvHorizontalAdapter;
    private TvAdapter tvAdapter;
    private SearchView searchFilm;
    private EditText searchEditText;
    private List<ModelTv> tvPlayNow = new ArrayList<>();
    private List<ModelTv> tvPopular = new ArrayList<>();
    private Map<Integer, String> genreMap = new HashMap<>();

    private Button btnLoadMore;
    private ProgressBar progressBar;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isSearching = false;
    private ChipGroup chipGroupGenres;
    private TextView text_rekomendasi_tv;
    private int currentSelectedGenreId = 0;

    public FragmentTv() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_film, container, false);

        progressBar = view.findViewById(R.id.progressBar);

        rvFilmRecommend = view.findViewById(R.id.rvFilmRecommend);
        rvFilmRecommend.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFilmRecommend.setHasFixedSize(true);

        rvNowPlaying = view.findViewById(R.id.rvNowPlaying);
        rvNowPlaying.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(rvNowPlaying);

        searchFilm = view.findViewById(R.id.searchFilm);
        searchFilm.setIconifiedByDefault(false);

        // editText untuk mengubah warna teks pencarian
        searchEditText = searchFilm.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));


        chipGroupGenres = view.findViewById(R.id.chipGroupGenres);
        chipGroupGenres.setSingleSelection(true);
        chipGroupGenres.setSelectionRequired(true);

        text_rekomendasi_tv = view.findViewById(R.id.text_rekomendasi_film);

        btnLoadMore = view.findViewById(R.id.btnLoadMore);
        btnLoadMore.setOnClickListener(v -> loadMoreTv());

        fetchTVGenres();

        chipGroupGenres.setOnCheckedStateChangeListener((chipGroup, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                Chip allChip = chipGroup.findViewWithTag(Integer.valueOf(0));
                if (allChip != null) {
                    allChip.setChecked(true);
                }
                currentPage = 1;
                currentSelectedGenreId = 0;
                fetchTVPopular(0);
            } else {
                int checkedViewId = checkedIds.get(0);
                Chip selectedChip = chipGroup.findViewById(checkedViewId);
                if (selectedChip != null && selectedChip.getTag() instanceof Integer) {
                    int genreId = (Integer) selectedChip.getTag();
                    if (genreId != currentSelectedGenreId) {
                        isSearching = false;
                        searchFilm.setQuery("", false);
                        currentPage = 1;
                        currentSelectedGenreId = genreId;
                        fetchTVPopular(genreId);
                    }
                }
            }
        });

        searchFilm.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().isEmpty()) {
                    isSearching = false;
                    currentPage = 1;
                    Chip selectedChip = chipGroupGenres.findViewWithTag(currentSelectedGenreId);
                    if (selectedChip != null) selectedChip.setChecked(true);
                    fetchTVPopular(currentSelectedGenreId);
                } else {
                    isSearching = true;
                    chipGroupGenres.clearCheck();
                    searchTv(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty() && isSearching) {
                    isSearching = false;
                    currentPage = 1;
                    Chip selectedChip = chipGroupGenres.findViewWithTag(currentSelectedGenreId);
                    if (selectedChip != null) selectedChip.setChecked(true);
                    fetchTVPopular(currentSelectedGenreId);
                }
                return false;
            }
        });

        searchFilm.setOnCloseListener(() -> {
            isSearching = false;
            currentPage = 1;
            Chip selectedChip = chipGroupGenres.findViewWithTag(currentSelectedGenreId);
            if (selectedChip != null) selectedChip.setChecked(true);
            fetchTVPopular(currentSelectedGenreId);
            return false;
        });

        fetchTVNowPlaying();
        fetchTVPopular(0);

        return view;
    }

    /** Ambil genre TV dari API dan tampilkan chip-nya */
    private void fetchTVGenres() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<GenreListResponse> call = apiService.getTVGenres(ApiConfig.API_KEY, ApiConfig.LANGUAGE);

        call.enqueue(new Callback<GenreListResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenreListResponse> call, @NonNull Response<GenreListResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Genre> genres = response.body().getGenres();
                    if (genres != null) {
                        genreMap.clear();
                        for (Genre genre : genres) {
                            genreMap.put(genre.getId(), genre.getName());
                        }
                        addGenresToChipGroup(genres);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load genres: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenreListResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error loading genres: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Tambah chip genre ke ChipGroup, termasuk "All" */

    @SuppressLint("SetTextI18n")
    private void addGenresToChipGroup(List<Genre> genres) {
        chipGroupGenres.removeAllViews();

        Chip allChip = new Chip(getContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setTag(0);
        allChip.setOnClickListener(v -> {
            if (allChip.isChecked() && chipGroupGenres.getCheckedChipIds().size() == 1) {
                allChip.setChecked(true);
            }
        });
        chipGroupGenres.addView(allChip);

        for (Genre genre : genres) {
            Chip chip = new Chip(getContext());
            chip.setText(genre.getName());
            chip.setCheckable(true);
            chip.setTag(genre.getId());
            chipGroupGenres.addView(chip);
        }
    }

    /** Ambil daftar TV Popular berdasarkan genre */
    @SuppressLint({"SetTextI18n"})
    private void fetchTVPopular(int genreId) {
        if (isSearching) return;
        if (currentPage == 1) {
            tvPopular.clear();
            if (tvAdapter != null) tvAdapter.notifyDataSetChanged();
        }
        progressBar.setVisibility(View.VISIBLE);
        isLoading = true;

        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<TVResponse> call;
        if (genreId != 0) {
            call = apiService.getTVShowsByGenre(ApiConfig.API_KEY, ApiConfig.LANGUAGE, genreId, currentPage);
            text_rekomendasi_tv.setText("Recommended TV Shows");
        } else {
            call = apiService.getTVPopular(ApiConfig.API_KEY, ApiConfig.LANGUAGE, currentPage);
            text_rekomendasi_tv.setText("Recommended TV Shows");
        }

        call.enqueue(new Callback<TVResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVResponse> call, Response<TVResponse> response) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<ModelTv> tvs = response.body().getResults();
                    if (tvs != null && !tvs.isEmpty()) {
                        for (ModelTv tv : tvs) {
                            List<String> names = new ArrayList<>();
                            if (tv.getGenreIds() != null) {
                                for (Integer id : tv.getGenreIds()) {
                                    String name = genreMap.get(id);
                                    if (name != null) names.add(name);
                                }
                            }
                            tv.setGenreNames(names);
                        }
                        tvPopular.addAll(tvs);
                        showFilmTV(false);
                    } else {
                        Toast.makeText(getContext(), "No TV shows found for this genre.", Toast.LENGTH_SHORT).show();
                        tvPopular.clear();
                        showFilmTV(false);
                    }
                    if (response.body().getTotalPages() > currentPage) {
                        btnLoadMore.setVisibility(View.VISIBLE);
                    } else {
                        btnLoadMore.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load TV: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                Toast.makeText(getContext(), "Network error TV: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTVNowPlaying() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<TVResponse> call = apiService.getTVNowPlaying(ApiConfig.API_KEY, ApiConfig.LANGUAGE);

        call.enqueue(new Callback<TVResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVResponse> call, @NonNull Response<TVResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    tvPlayNow.clear();
                    List<ModelTv> result = response.body().getResults();
                    for (ModelTv tv : result) {
                        List<String> names = new ArrayList<>();
                        if (tv.getGenreIds() != null) {
                            for (Integer id : tv.getGenreIds()) {
                                String name = genreMap.get(id);
                                if (name != null) names.add(name);
                            }
                        }
                        tv.setGenreNames(names);
                    }
                    tvPlayNow.addAll(result);
                    showTVHorizontal();
                }
            }

            @Override
            public void onFailure(Call<TVResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error Now Playing TV: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchTv(String query) {
        progressBar.setVisibility(View.VISIBLE);
        isLoading = true;

        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<TVResponse> call = apiService.searchTV(ApiConfig.API_KEY, ApiConfig.LANGUAGE, query);

        call.enqueue(new Callback<TVResponse>() {
            @Override
            public void onResponse(@NonNull Call<TVResponse> call, @NonNull Response<TVResponse> response) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    tvPopular.clear();
                    List<ModelTv> result = response.body().getResults();
                    for (ModelTv tv : result) {
                        List<String> names = new ArrayList<>();
                        if (tv.getGenreIds() != null) {
                            for (Integer id : tv.getGenreIds()) {
                                String name = genreMap.get(id);
                                if (name != null) names.add(name);
                            }
                        }
                        tv.setGenreNames(names);
                    }
                    tvPopular.addAll(result);
                    showFilmTV(true);
                    text_rekomendasi_tv.setText("Search results for: " + query);
                    btnLoadMore.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getContext(), "No search results for '" + query + "'.", Toast.LENGTH_SHORT).show();
                    tvPopular.clear();
                    showFilmTV(true);
                    text_rekomendasi_tv.setText("No results. Try another keyword.");
                    btnLoadMore.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TVResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                Toast.makeText(getContext(), "Network error during search: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMoreTv() {
        if (!isLoading && !isSearching) {
            currentPage++;
            fetchTVPopular(currentSelectedGenreId);
        } else if (isSearching) {
            Toast.makeText(getContext(), "Cannot load more while search mode is active.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTVHorizontal() {
        tvHorizontalAdapter = new TvHorizontalAdapter(getActivity(), tvPlayNow, this);
        rvNowPlaying.setAdapter(tvHorizontalAdapter);
        tvHorizontalAdapter.notifyDataSetChanged();
    }

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