package com.example.tmflix.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.tmflix.R;
import com.example.tmflix.adapter.ViewPageAdapter;
import com.google.android.material.tabs.TabLayout;

public class FragmentFavorite extends Fragment {

    public FragmentFavorite() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite, container, false);

        ViewPager viewPager = rootView.findViewById(R.id.vpFavorite);
        viewPager.setAdapter(new ViewPageAdapter(getChildFragmentManager()));

        TabLayout tabLayout = rootView.findViewById(R.id.tabFavorite);
        tabLayout.setupWithViewPager(viewPager);

        if (tabLayout.getTabAt(0) != null) {
            tabLayout.getTabAt(0).setText("Movie");
        }

        if (tabLayout.getTabAt(1) != null) {
            tabLayout.getTabAt(1).setText("TV Show");
        }

        return rootView;
    }
}
