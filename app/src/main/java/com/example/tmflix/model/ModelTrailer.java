package com.example.tmflix.model;

import java.io.Serializable;

public class ModelTrailer implements Serializable {

    private String key;
    private String type;

    public ModelTrailer() {
        // Konstruktor kosong diperlukan untuk deserialisasi
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
