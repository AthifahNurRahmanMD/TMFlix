package com.example.tmflix.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tmflix.R;
import com.example.tmflix.activities.DetailTelevisionActivity;
import com.example.tmflix.adapter.TvAdapter;
import com.example.tmflix.model.ModelTv;
import com.example.tmflix.sqlite.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavoriteTv extends Fragment implements TvAdapter.onSelectData {

    private RecyclerView rvMovieFav;
    private List<ModelTv> modelTV = new ArrayList<>();
    private DatabaseHelper helper;
    private TextView txtNoData;

    public FragmentFavoriteTv() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite_film, container, false);

        helper = new DatabaseHelper(getActivity());  // Inisialisasi DatabaseHelper

        txtNoData = rootView.findViewById(R.id.tvNotFound);
        rvMovieFav = rootView.findViewById(R.id.rvMovieFav);
        rvMovieFav.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvMovieFav.setAdapter(new TvAdapter(getActivity(), modelTV, this));
        rvMovieFav.setHasFixedSize(true);

        setData();
        return rootView;
    }

    private void setData() {
        modelTV = helper.getAllFavoriteTv();  // Ganti method RealmHelper ke method SQLite
        if (modelTV.size() == 0) {
            txtNoData.setVisibility(View.VISIBLE);
            rvMovieFav.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            rvMovieFav.setVisibility(View.VISIBLE);
            rvMovieFav.setAdapter(new TvAdapter(getActivity(), modelTV, this));
        }
    }

    @Override
    public void onSelected(ModelTv modelTV) {
        Intent intent = new Intent(getActivity(), DetailTelevisionActivity.class);
        intent.putExtra("detailTV", modelTV);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        setData();
    }
}
