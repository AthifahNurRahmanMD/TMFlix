package com.example.tmflix.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TVResponse {
    @SerializedName("results")
    private List<com.example.tmflix.model.ModelTv> results;

    @SerializedName("total_pages")
    private int totalPages;

    public List<com.example.tmflix.model.ModelTv> getResults() {
        return results;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
