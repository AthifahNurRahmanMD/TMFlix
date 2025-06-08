package com.example.tmflix.networking;

import com.example.tmflix.BuildConfig;

public class ApiConfig {

        // Konfigurasi Umum
        public static final String BASE_URL = "https://api.themoviedb.org/3/";
        public static final String API_KEY = BuildConfig.TMDB_API_KEY;
        public static final String LANGUAGE = "en-US";
        public static final String URL_IMAGE = "https://image.tmdb.org/t/p/w780/";
//        public static final String URL_IMAGE = "https://www.themoviedb.org/tv/";
        public static final String URL_FILM = "https://www.themoviedb.org/movie/";

        // Endpoint Path
        public static final String MOVIE_PLAY_NOW = "movie/now_playing";
        public static final String MOVIE_POPULAR = "discover/movie";
        public static final String TV_PLAY_NOW = "tv/on_the_air";
        public static final String TV_POPULAR = "discover/tv";
        public static final String SEARCH_MOVIE = "search/movie";
        public static final String SEARCH_TV = "search/tv";
        public static final String MOVIE_VIDEO = "movie/{id}/videos";
        public static final String TV_VIDEO = "tv/{id}/videos";

        // Query Parameter tambahan
        public static final String NOTIF_DATE = "primary_release_date.gte";
        public static final String RELEASE_DATE = "primary_release_date.lte";
}
