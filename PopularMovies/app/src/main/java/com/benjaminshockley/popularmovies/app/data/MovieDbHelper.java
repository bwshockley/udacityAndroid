/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.benjaminshockley.popularmovies.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.benjaminshockley.popularmovies.app.data.MovieContract.FavMoviesEntry;
import com.benjaminshockley.popularmovies.app.data.MovieContract.TrailersEntry;
import com.benjaminshockley.popularmovies.app.data.MovieContract.ReviewsEntry;


/**
 * Manages a local database for movie data.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "favoriteMovies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + FavMoviesEntry.TABLE_NAME + " (" +

                // This is the primary key, the movie ID.
                FavMoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                // The rest of the location data.
                FavMoviesEntry.COLUMN_MOVIE_ID + " REAL NOT NULL, " +
                FavMoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                FavMoviesEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                FavMoviesEntry.COLUMN_MOVIE_RUNTIME + " TEXT NOT NULL, " +
                FavMoviesEntry.COLUMN_MOVIE_VOTE_AVERAGE + " REAL NOT NULL," +
                FavMoviesEntry.COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                FavMoviesEntry.COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL," +
                FavMoviesEntry.COLUMN_MOVIE_LOCAL_POSTER_PATH + " TEXT NOT NULL," +
                FavMoviesEntry.COLUMN_MOVIE_BACKDROP_PATH + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);

        final String SQL_CREATE_VIDEOS_TABLE = "CREATE TABLE " + TrailersEntry.TABLE_NAME + " (" +

                TrailersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // The ID of the movie associated with this video data.
                TrailersEntry.COLUMN_MOVIE_KEY + " REAL NOT NULL, " +

                // The rest of the videos table columns.
                TrailersEntry.COLUMN_TRAILER_NAME + " TEXT NOT NULL, " +
                TrailersEntry.COLUMN_TRAILER_SIZE + " TEXT NOT NULL, " +
                TrailersEntry.COLUMN_TRAILER_SOURCE + " TEXT NOT NULL, " +
                TrailersEntry.COLUMN_TRAILER_TYPE + " TEXT NOT NULL," +

                // Set up the movie column as a foreign key to videos table.
                " FOREIGN KEY (" + TrailersEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                FavMoviesEntry.TABLE_NAME + " (" + FavMoviesEntry.COLUMN_MOVIE_ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_VIDEOS_TABLE);

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewsEntry.TABLE_NAME + " (" +

                ReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // The ID of the movie associated with this video data.
                ReviewsEntry.COLUMN_MOVIE_KEY + " REAL NOT NULL, " +

                // The rest of the reviews table columns.
                ReviewsEntry.COLUMN_REVIEW_ID + " REAL NOT NULL, " +
                ReviewsEntry.COLUMN_REVIEW_AUTHOR + " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_REVIEW_CONTENT + " TEXT NOT NULL," +
                ReviewsEntry.COLUMN_REVIEW_URL + " TEXT NOT NULL," +

                // Set up the movie column as a foreign key to reviews table.
                " FOREIGN KEY (" + TrailersEntry.COLUMN_MOVIE_KEY + ") REFERENCES " +
                FavMoviesEntry.TABLE_NAME + " (" + FavMoviesEntry.COLUMN_MOVIE_ID + "));";

        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavMoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailersEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
