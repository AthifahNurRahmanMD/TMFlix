package com.example.tmflix.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.tmflix.model.ModelMovie;
import com.example.tmflix.model.ModelTv;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tmflix.db";
    private static final int DATABASE_VERSION = 1;

    // ======== TABEL MOVIE ========
    private static final String TABLE_MOVIE = "favorite_movie";
    private static final String MOVIE_ID = "id";
    private static final String MOVIE_TITLE = "title";
    private static final String MOVIE_VOTE_AVERAGE = "vote_average";
    private static final String MOVIE_OVERVIEW = "overview";
    private static final String MOVIE_RELEASE_DATE = "release_date";
    private static final String MOVIE_POSTER_PATH = "poster_path";
    private static final String MOVIE_BACKDROP_PATH = "backdrop_path";
    private static final String MOVIE_POPULARITY = "popularity";

    // ======== TABEL TV ========
    private static final String TABLE_TV = "favorite_tv";
    private static final String TV_ID = "id";
    private static final String TV_NAME = "name";
    private static final String TV_VOTE_AVERAGE = "vote_average";
    private static final String TV_OVERVIEW = "overview";
    private static final String TV_RELEASE_DATE = "release_date";
    private static final String TV_POSTER_PATH = "poster_path";
    private static final String TV_BACKDROP_PATH = "backdrop_path";
    private static final String TV_POPULARITY = "popularity";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createMovieTable = "CREATE TABLE " + TABLE_MOVIE + " ("
                + MOVIE_ID + " INTEGER PRIMARY KEY, "
                + MOVIE_TITLE + " TEXT, "
                + MOVIE_VOTE_AVERAGE + " REAL, "
                + MOVIE_OVERVIEW + " TEXT, "
                + MOVIE_RELEASE_DATE + " TEXT, "
                + MOVIE_POSTER_PATH + " TEXT, "
                + MOVIE_BACKDROP_PATH + " TEXT, "
                + MOVIE_POPULARITY + " REAL)";
        String createTVTable = "CREATE TABLE " + TABLE_TV + " ("
                + TV_ID + " INTEGER PRIMARY KEY, "
                + TV_NAME + " TEXT, "
                + TV_VOTE_AVERAGE + " REAL, "
                + TV_OVERVIEW + " TEXT, "
                + TV_RELEASE_DATE + " TEXT, "
                + TV_POSTER_PATH + " TEXT, "
                + TV_BACKDROP_PATH + " TEXT, "
                + TV_POPULARITY + " REAL)";
        db.execSQL(createMovieTable);
        db.execSQL(createTVTable);
    }

    /** Upgrade: hapus tabel lama, lalu buat ulang */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TV);
        onCreate(db);
    }

    // ===========================
    // OPERASI PADA TABEL MOVIE
    // ===========================

    /** Menambahkan movie ke tabel favorite_movie */
    public long insertFavoriteMovie(ModelMovie movie) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MOVIE_ID, movie.getId());
        values.put(MOVIE_TITLE, movie.getTitle());
        values.put(MOVIE_VOTE_AVERAGE, movie.getVoteAverage());
        values.put(MOVIE_OVERVIEW, movie.getOverview());
        values.put(MOVIE_RELEASE_DATE, movie.getReleaseDate());
        values.put(MOVIE_POSTER_PATH, movie.getPosterPath());
        values.put(MOVIE_BACKDROP_PATH, movie.getBackdropPath());
        values.put(MOVIE_POPULARITY, movie.getPopularity());

        long result = db.insert(TABLE_MOVIE, null, values);
        db.close();
        return result; // = -1 jika gagal
    }

    /** Menghapus movie dari tabel favorite_movie berdasar ID */
    public int deleteFavoriteMovie(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(
                TABLE_MOVIE,
                MOVIE_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        db.close();
        return result; // jumlah baris yang terhapus
    }

    /** Mengambil semua favorite movie */
    public List<ModelMovie> getAllFavoriteMovies() {
        List<ModelMovie> movieList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MOVIE + " ORDER BY " + MOVIE_TITLE + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ModelMovie movie = new ModelMovie();
                movie.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MOVIE_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_TITLE)));
                movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndexOrThrow(MOVIE_VOTE_AVERAGE)));
                movie.setOverview(cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_OVERVIEW)));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_RELEASE_DATE)));
                movie.setPosterPath(cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_POSTER_PATH)));
                movie.setBackdropPath(cursor.getString(cursor.getColumnIndexOrThrow(MOVIE_BACKDROP_PATH)));
                movie.setPopularity(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(MOVIE_POPULARITY))));

                movieList.add(movie);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return movieList;
    }

    /** Mengecek apakah sebuah movie sudah ada di favorit (berdasarkan ID) */
    public boolean isMovieFavorited(int id) {
        String selectQuery = "SELECT 1 FROM " + TABLE_MOVIE +
                " WHERE " + MOVIE_ID + " = " + id + " LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    // ===========================
    // OPERASI PADA TABEL TV
    // ===========================

    /** Menambahkan TV ke tabel favorite_tv */
    public long insertFavoriteTv(ModelTv tv) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TV_ID, tv.getId());
        values.put(TV_NAME, tv.getName());
        values.put(TV_VOTE_AVERAGE, tv.getVoteAverage());
        values.put(TV_OVERVIEW, tv.getOverview());
        values.put(TV_RELEASE_DATE, tv.getReleaseDate());
        values.put(TV_POSTER_PATH, tv.getPosterPath());
        values.put(TV_BACKDROP_PATH, tv.getBackdropPath());
        values.put(TV_POPULARITY, tv.getPopularity());

        long result = db.insert(TABLE_TV, null, values);
        db.close();
        return result;
    }

    /** Menghapus TV dari tabel favorite_tv berdasar ID */
    public int deleteFavoriteTv(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(
                TABLE_TV,
                TV_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        db.close();
        return result;
    }

    /** Mengambil semua favorite TV */
    public List<ModelTv> getAllFavoriteTv() {
        List<ModelTv> tvList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TV + " ORDER BY " + TV_NAME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ModelTv tv = new ModelTv();
                tv.setId(cursor.getInt(cursor.getColumnIndexOrThrow(TV_ID)));
                tv.setName(cursor.getString(cursor.getColumnIndexOrThrow(TV_NAME)));
                tv.setVoteAverage(cursor.getDouble(cursor.getColumnIndexOrThrow(TV_VOTE_AVERAGE)));
                tv.setOverview(cursor.getString(cursor.getColumnIndexOrThrow(TV_OVERVIEW)));
                tv.setReleaseDate(cursor.getString(cursor.getColumnIndexOrThrow(TV_RELEASE_DATE)));
                tv.setPosterPath(cursor.getString(cursor.getColumnIndexOrThrow(TV_POSTER_PATH)));
                tv.setBackdropPath(cursor.getString(cursor.getColumnIndexOrThrow(TV_BACKDROP_PATH)));
                tv.setPopularity(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(TV_POPULARITY))));

                tvList.add(tv);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return tvList;
    }

    /** Mengecek apakah sebuah TV sudah di‐favoritkan (berdasarkan ID) */
    public boolean isTvFavorited(int id) {
        String selectQuery = "SELECT 1 FROM " + TABLE_TV +
                " WHERE " + TV_ID + " = " + id + " LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }




}
