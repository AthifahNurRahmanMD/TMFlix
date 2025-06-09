package com.example.tmflix.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tmflix.R;
import com.example.tmflix.activities.DetailMovieActivity;
import com.example.tmflix.adapter.MovieAdapter;
import com.example.tmflix.model.ModelMovie;
import com.example.tmflix.sqlite.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

public class FragmentFavoriteMovie extends Fragment implements MovieAdapter.onSelectData {

    private RecyclerView rvMovieFav;
    private List<ModelMovie> modelMovie = new ArrayList<>();
    private DatabaseHelper helper;
    private TextView txtNoData;

    public FragmentFavoriteMovie() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite_film, container, false);

        helper = new DatabaseHelper(requireContext());  // Inisialisasi DatabaseHelper

        txtNoData = rootView.findViewById(R.id.tvNotFound);
        rvMovieFav = rootView.findViewById(R.id.rvMovieFav);

        rvMovieFav.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvMovieFav.setHasFixedSize(true);
        rvMovieFav.setAdapter(new MovieAdapter(getActivity(), modelMovie, this));

        setData();

        return rootView;
    }

    private void setData() {
        modelMovie = helper.getAllFavoriteMovies();
        if (modelMovie == null || modelMovie.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            rvMovieFav.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            rvMovieFav.setVisibility(View.VISIBLE);
            rvMovieFav.setAdapter(new MovieAdapter(getActivity(), modelMovie, this));
        }
    }

    @Override
    public void onSelected(ModelMovie modelMovie) {
        Intent intent = new Intent(getActivity(), DetailMovieActivity.class);
        intent.putExtra("detailMovie", modelMovie);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        setData();
    }
}
