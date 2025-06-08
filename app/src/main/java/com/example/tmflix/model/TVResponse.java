package com.example.tmflix.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TVResponse {
    @SerializedName("results")
    private List<com.example.tmflix2.model.ModelTv> results;

    public List<com.example.tmflix2.model.ModelTv> getResults() {
        return results;
    }

    public void setResults(List<com.example.tmflix2.model.ModelTv> results) {
        this.results = results;
    }

    @SerializedName("total_pages")
    private int totalPages;

    public int getTotalPages() {
        return totalPages;
    }
}
