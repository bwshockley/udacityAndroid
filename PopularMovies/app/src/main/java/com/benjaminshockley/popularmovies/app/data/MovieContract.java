package com.benjaminshockley.popularmovies.app.data;

import android.provider.BaseColumns;

/**
 * This is the movie contract.  It defines the database structure
 */
public class MovieContract {

    //Empty constructor for good measure.
    public MovieContract() {}


    /* Inner class that defines the table contents of the favorite movies table */
    public static final class FavMoviesEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "favoriteMovies";

        // The movie table columns.
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_TITLE = "movie_title";
        public static final String COLUMN_MOVIE_RELEASE_DATE = "movie_release_date";
        public static final String COLUMN_MOVIE_OVERVIEW = "movie_overview";
        public static final String COLUMN_MOVIE_POSTER_PATH = "movie_poster_path";
        public static final String COLUMN_MOVIE_LOCAL_POSTER_PATH = "movie_local_poster_path";
        public static final String COLUMN_MOVIE_BACKDROP_PATH = "movie_backdrop_path";
        public static final String COLUMN_MOVIE_RUNTIME = "movie_runtime";
        public static final String COLUMN_MOVIE_VOTE_AVERAGE = "movie_vote_average";

    }

    /* Inner class that defines the table contents of the videos table */
    public static final class TrailersEntry implements BaseColumns {

        public static final String TABLE_NAME = "trailers";

        // The Videos table columns.
        public static final String COLUMN_MOVIE_KEY = "movie_id";
        public static final String COLUMN_TRAILER_NAME = "trailer_name";
        public static final String COLUMN_TRAILER_SIZE = "trailer_size";
        public static final String COLUMN_TRAILER_SOURCE = "trailer_source";
        public static final String COLUMN_TRAILER_TYPE = "trailer_type";

    }

    /* Inner class that defines the table contents of the videos table */
    public static final class ReviewsEntry implements BaseColumns {

        public static final String TABLE_NAME = "reviews";

        // The reviews table columns.
        public static final String COLUMN_MOVIE_KEY = "movie_id";
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_REVIEW_AUTHOR = "review_author";
        public static final String COLUMN_REVIEW_CONTENT = "review_content";
        public static final String COLUMN_REVIEW_URL = "review_url";

    }
}