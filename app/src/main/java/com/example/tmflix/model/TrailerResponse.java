package com.example.tmflix.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TrailerResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("results")
    private List<com.example.tmflix2.model.ModelTrailer> results;

    public int getId() {
        return id;
    }

    public List<com.example.tmflix2.model.ModelTrailer> getResults() {
        return results;
    }
}
