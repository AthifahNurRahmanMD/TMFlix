package com.example.tmflix.networking;

import com.example.tmflix.model.GenreListResponse;
import com.example.tmflix.model.MovieResponse;
import com.example.tmflix.model.TVResponse;
import com.example.tmflix.model.TrailerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface ApiService {
    @GET(ApiConfig.MOVIE_VIDEO)
    Call<TrailerResponse> getMovieTrailers(
            @Path("id") int movieId,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET(ApiConfig.TV_VIDEO)
    Call<TrailerResponse> getTVTrailers(
            @Path("id") int tvId,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET(ApiConfig.MOVIE_PLAY_NOW)
    Call<MovieResponse> getNowPlaying(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET(ApiConfig.MOVIE_POPULAR)
    Call<MovieResponse> getPopularMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET(ApiConfig.SEARCH_MOVIE)
    Call<MovieResponse> searchMovie(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("query") String query
    );

    @GET(ApiConfig.TV_PLAY_NOW)
    Call<TVResponse> getTVNowPlaying(
            @Query("api_key") String apiKey,
            @Query("language") String language

    );

    @GET(ApiConfig.TV_POPULAR)
    Call<TVResponse> getTVPopular(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET(ApiConfig.SEARCH_TV)
    Call<TVResponse> searchTV(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("query") String query
    );

    @GET(ApiConfig.GENRE_MOVIE)
    Call<GenreListResponse> getMovieGenres(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET(ApiConfig.GENRE_TV)
    Call<GenreListResponse> getTVGenres(
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET(ApiConfig.DISCOVER_MOVIE_BY_GENRE)
    Call<MovieResponse> getMoviesByGenre(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("with_genres") int genreId,
            @Query("page") int page
    );

    @GET(ApiConfig.DISCOVER_TV_BY_GENRE)
    Call<TVResponse> getTVShowsByGenre(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("with_genres") int genreId,
            @Query("page") int page
    );
}