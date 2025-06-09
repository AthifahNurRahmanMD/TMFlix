package com.example.tmflix.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import com.example.tmflix.R;
import com.example.tmflix.activities.DetailMovieActivity;
import com.example.tmflix.adapter.MovieAdapter;
import com.example.tmflix.adapter.MovieHorizontalAdapter;
import com.example.tmflix.model.Genre;
import com.example.tmflix.model.ModelMovie;
import com.example.tmflix.model.MovieResponse;
import com.example.tmflix.model.GenreListResponse;
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

public class FragmentMovie extends Fragment {

    // Recycler untuk daftar film dan now playing (horizontal)
    private RecyclerView rvFilmRecommend, rvNowPlaying;
    private MovieAdapter movieAdapter;
    private MovieHorizontalAdapter adapterHorizontal;
    // List data film n genre
    private List<ModelMovie> listFilm = new ArrayList<>();
    private List<ModelMovie> listFilmHorizontal = new ArrayList<>();
    private Map<Integer, String> genreMap = new HashMap<>();

    // Button untuk load more
    private Button btnLoadMore;
    private ProgressBar progressBar;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isSearching = false; // Flag apakah sedang mode pencarian

    private TextView text_rekomendasi_film;
    private SearchView searchFilm;
    private ChipGroup chipGroupGenres;

    private int currentSelectedGenreId = 0; // ID genre yang sedang aktif (0 = "All")


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_film, container, false);

        // Inisialisasi komponen UI
        progressBar = view.findViewById(R.id.progressBar);

        // Setup recyclerView untuk rekomendasi film (vertikal)
        rvFilmRecommend = view.findViewById(R.id.rvFilmRecommend);
        rvFilmRecommend.setLayoutManager(new LinearLayoutManager(getContext()));
        movieAdapter = new MovieAdapter(getContext(), listFilm, modelMovie -> {
            // Navigasi ke detail saat film dipilih
            Intent intent = new Intent(getContext(), DetailMovieActivity.class);
            intent.putExtra("detailMovie", modelMovie);
            startActivity(intent);
        });
        rvFilmRecommend.setAdapter(movieAdapter);

        // Setup recyclerView horizontal untuk now playing
        rvNowPlaying = view.findViewById(R.id.rvNowPlaying);
        rvNowPlaying.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        adapterHorizontal = new MovieHorizontalAdapter(getContext(), listFilmHorizontal, modelMovie -> {
            Intent intent = new Intent(getContext(), DetailMovieActivity.class);
            intent.putExtra("detailMovie", modelMovie);
            startActivity(intent);
        });
        rvNowPlaying.setAdapter(adapterHorizontal);
        // Snap ke tengah saat scroll (UX bagus)
        new LinearSnapHelper().attachToRecyclerView(rvNowPlaying);

        // SearchView setup
        searchFilm = view.findViewById(R.id.searchFilm);
        searchFilm.setIconifiedByDefault(false);

        chipGroupGenres = view.findViewById(R.id.chipGroupGenres);
        chipGroupGenres.setSingleSelection(true);
        chipGroupGenres.setSelectionRequired(true);

        text_rekomendasi_film = view.findViewById(R.id.text_rekomendasi_film);

        btnLoadMore = view.findViewById(R.id.btnLoadMore);
        btnLoadMore.setOnClickListener(v -> loadMoreMovies());

        // Ambil data awal
        fetchMovieGenres();
        fetchNowPlayingMovies();
        fetchMovies(0); // Mulai dengan genre "All"

        // Listener untuk chip genre
        chipGroupGenres.setOnCheckedStateChangeListener((chipGroup, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // Jika tidak ada chip aktif, set chip "All" aktif (tidak boleh tidak aktif)
                Chip allChip = chipGroup.findViewWithTag(Integer.valueOf(0));
                if (allChip != null) {
                    allChip.setChecked(true);
                }
                currentPage = 1;
                currentSelectedGenreId = 0;
                fetchMovies(0);
            } else {
                // Ambil genreId dari tag chip yang dipilih (checkedIds berisi id view, bukan genreId)
                int checkedViewId = checkedIds.get(0);
                Chip selectedChip = chipGroup.findViewById(checkedViewId);
                if (selectedChip != null && selectedChip.getTag() instanceof Integer) {
                    int genreId = (Integer) selectedChip.getTag();
                    if (genreId != currentSelectedGenreId) {
                        isSearching = false;
                        searchFilm.setQuery("", false); // Kosongkan query
                        currentPage = 1;
                        currentSelectedGenreId = genreId;
                        fetchMovies(genreId);
                    }
                }
            }
        });

        // Listener untuk search submit dan perubahan teks
        searchFilm.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().isEmpty()) {
                    // Jika query kosong, kembali ke tampilan genre
                    isSearching = false;
                    currentPage = 1;
                    Chip selectedChip = chipGroupGenres.findViewWithTag(currentSelectedGenreId);
                    if (selectedChip != null) selectedChip.setChecked(true);
                    fetchMovies(currentSelectedGenreId);
                } else {
                    isSearching = true;
                    chipGroupGenres.clearCheck(); // Uncheck semua chip saat mencari
                    searchMovie(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Jika teks pencarian dihapus setelah mode search, kembali ke genre sebelumnya
                if (newText.isEmpty() && isSearching) {
                    isSearching = false;
                    currentPage = 1;
                    Chip selectedChip = chipGroupGenres.findViewWithTag(currentSelectedGenreId);
                    if (selectedChip != null) selectedChip.setChecked(true);
                    fetchMovies(currentSelectedGenreId);
                }
                return false;
            }
        });

        // Listener tombol silang pada SearchView
        searchFilm.setOnCloseListener(() -> {
            isSearching = false;
            currentPage = 1;
            Chip selectedChip = chipGroupGenres.findViewWithTag(currentSelectedGenreId);
            if (selectedChip != null) selectedChip.setChecked(true);
            fetchMovies(currentSelectedGenreId);
            return false;
        });

        return view;
    }

   // ambil ffilm yg ut horizontal
    private void fetchNowPlayingMovies() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<MovieResponse> call = apiService.getNowPlaying(ApiConfig.API_KEY, ApiConfig.LANGUAGE);

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    listFilmHorizontal.clear();
                    if (response.body().getResults() != null) {
                        listFilmHorizontal.addAll(response.body().getResults());
                    }
                    adapterHorizontal.notifyDataSetChanged();
                } else {
                    Log.e("FragmentMovie", "Gagal memuat Now Playing: " + response.message());
                    Toast.makeText(getContext(), "Gagal memuat Now Playing: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("FragmentMovie", "Error memuat Now Playing: " + t.getMessage());
                Toast.makeText(getContext(), "Error jaringan Now Playing: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    //Ambil daftar genre dari API dan tampilkan chip-nya
    private void fetchMovieGenres() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<GenreListResponse> call = apiService.getMovieGenres(ApiConfig.API_KEY, ApiConfig.LANGUAGE);

        call.enqueue(new Callback<GenreListResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenreListResponse> call, @NonNull Response<GenreListResponse> response) {
                progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                    List<Genre> genres = response.body().getGenres();
                    if (genres != null) {
                        genreMap.clear(); // SIMPAN MAP GENRE
                        for (Genre genre : genres) {
                            genreMap.put(genre.getId(), genre.getName());
                        }
                        addGenresToChipGroup(genres);
                    }
                }else {
                    Log.e("FragmentMovie", "Gagal memuat genre: " + response.message());
                    Toast.makeText(getContext(), "Gagal memuat genre: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenreListResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("FragmentMovie", "Error memuat genre: " + t.getMessage());
                Toast.makeText(getContext(), "Error jaringan genre: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Menambah chip genre ke ChipGroup, termasuk chip "All" yang selalu aktif saat tidak ada genre lain aktif
     */
    private void addGenresToChipGroup(List<Genre> genres) {
        chipGroupGenres.removeAllViews();

        // Chip "All" (default aktif, tidak bisa di-uncheck manual)
        Chip allChip = new Chip(getContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setTag(0);
        // Prevent manual uncheck "All" chip: jika satu-satunya yang aktif, tidak bisa di-uncheck
        allChip.setOnClickListener(v -> {
            if (allChip.isChecked() && chipGroupGenres.getCheckedChipIds().size() == 1) {
                allChip.setChecked(true);
            }
        });
        chipGroupGenres.addView(allChip);

        // Chip untuk setiap genre
        for (Genre genre : genres) {
            Chip chip = new Chip(getContext());
            chip.setText(genre.getName());
            chip.setCheckable(true);
            chip.setTag(genre.getId());
            chipGroupGenres.addView(chip);
        }
    }

    //Ambil film berdasarkan genreId

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void fetchMovies(int genreId) {
        if (isSearching) return; // Jangan fetch genre jika sedang pencarian

        // Hanya kosongkan list saat load awal (bukan load more)
        if (currentPage == 1) {
            listFilm.clear();
            movieAdapter.notifyDataSetChanged();
        }

        progressBar.setVisibility(View.VISIBLE);
        isLoading = true;

        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<MovieResponse> call;

        if (genreId != 0) {
            call = apiService.getMoviesByGenre(ApiConfig.API_KEY, ApiConfig.LANGUAGE, genreId, currentPage);
            text_rekomendasi_film.setText("Recommended Movies");
        } else {
            call = apiService.getPopularMovies(ApiConfig.API_KEY, ApiConfig.LANGUAGE, currentPage);
            text_rekomendasi_film.setText("Recommended Movies");
        }

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;

//                if (response.isSuccessful() && response.body() != null) {
//                    if (currentPage == 1) {
//                        listFilm.clear();
//                    }
//
//                    if (response.body().getResults() != null && !response.body().getResults().isEmpty()) {
//                        listFilm.addAll(response.body().getResults());
//                        movieAdapter.notifyDataSetChanged();
//                    }
                if (response.isSuccessful() && response.body() != null) {
                    if (currentPage == 1) {
                        listFilm.clear();
                    }
                    List<ModelMovie> movies = response.body().getResults();
                    if (movies != null && !movies.isEmpty()) {
                        // --- MAPPING genreIds ke genreNames ---
                        for (ModelMovie movie : movies) {
                            List<String> names = new ArrayList<>();
                            if (movie.getGenreIds() != null) {
                                for (Integer id : movie.getGenreIds()) {
                                    String name = genreMap.get(id);
                                    if (name != null) names.add(name);
                                }
                            }
                            movie.setGenreNames(names);
                            movieAdapter.notifyDataSetChanged();
                        }
                        listFilm.addAll(movies);
                        movieAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Tidak ada film yang ditemukan untuk genre ini.", Toast.LENGTH_SHORT).show();
                        listFilm.clear();
                        movieAdapter.notifyDataSetChanged();
                    }

                    // Tampilkan tombol load more jika masih ada halaman berikutnya
                    if (response.body().getTotalPages() > currentPage) {
                        btnLoadMore.setVisibility(View.VISIBLE);
                    } else {
                        btnLoadMore.setVisibility(View.GONE);
                    }
                } else {
                    Log.e("MovieFragment", "Gagal memuat film: " + response.message());
                    Toast.makeText(getContext(), "Gagal memuat film: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                Log.e("MovieFragment", "Error memuat film: " + t.getMessage());
                Toast.makeText(getContext(), "Error jaringan film: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchMovie(String query) {
        progressBar.setVisibility(View.VISIBLE);
        isLoading = true;

        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);
        Call<MovieResponse> call = apiService.searchMovie(ApiConfig.API_KEY, ApiConfig.LANGUAGE, query);

        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call, @NonNull Response<MovieResponse> response) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    listFilm.clear();
                    if (response.body().getResults() != null && !response.body().getResults().isEmpty()) {
                        listFilm.addAll(response.body().getResults());
                        movieAdapter.notifyDataSetChanged();
                        text_rekomendasi_film.setText("Hasil Pencarian untuk: " + query);
                    } else {
                        Toast.makeText(getContext(), "Tidak ada hasil pencarian untuk '" + query + "'.", Toast.LENGTH_SHORT).show();
                        listFilm.clear();
                        movieAdapter.notifyDataSetChanged();
                        text_rekomendasi_film.setText("Tidak ada hasil. Coba kata kunci lain.");
                    }
                    btnLoadMore.setVisibility(View.GONE);
                } else {
                    Log.e("MovieFragment", "Gagal mencari film: " + response.message());
                    Toast.makeText(getContext(), "Gagal mencari film: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                isLoading = false;
                Log.e("MovieFragment", "Error mencari film: " + t.getMessage());
                Toast.makeText(getContext(), "Error jaringan pencarian: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMoreMovies() {
        if (!isLoading && !isSearching) {
            currentPage++;
            fetchMovies(currentSelectedGenreId);
        } else if (isSearching) {
            Toast.makeText(getContext(), "Tidak bisa memuat lebih banyak saat mode pencarian aktif.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
    }
}