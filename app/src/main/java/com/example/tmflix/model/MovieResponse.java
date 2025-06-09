package com.example.tmflix.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieResponse {
    @SerializedName("results")
    private List<com.example.tmflix.model.ModelMovie> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    public List<com.example.tmflix.model.ModelMovie> getResults() {
        return results;
    }

    public void setResults(List<com.example.tmflix.model.ModelMovie> results) {
        this.results = results;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) { //
        this.totalResults = totalResults;
    }
}