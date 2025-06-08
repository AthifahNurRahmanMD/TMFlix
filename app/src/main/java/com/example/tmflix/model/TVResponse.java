package com.example.tmflix.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TVResponse {
    @SerializedName("results")
    private List<ModelTv> results;

    public List<ModelTv> getResults() {
        return results;
    }

    public void setResults(List<ModelTv> results) {
        this.results = results;
    }
}
