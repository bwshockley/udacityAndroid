package com.benjaminshockley.popularmovies.app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.benjaminshockley.popularmovies.app.data.MovieContract;
import com.benjaminshockley.popularmovies.app.data.MovieDbHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by benjaminshockley on 9/1/15.
 *
 * Utility class to hold methods used throughout the app.
 */
public class Utility {

    private static MovieDbHelper mdbHelper = null;

    public static void populateDetails(
            final Context context,
            LinearLayout rootLinearLayout,
            Movie mMovie,
            ArrayList<Review> reviews,
            ArrayList<Trailer> trailers
    ) {

        //Update the runtimeView text.
        DetailActivityFragment.runtimeView.setText(mMovie.getRuntime() + " min");

        // Create the trailers views and the review views and populate them.
        if (trailers.size() > 0) {
            TextView trailersHeader = new TextView(context);
            trailersHeader.setText("Trailers");
            trailersHeader.setTextAppearance(context, R.style.trailerHeader);
            trailersHeader.setBackgroundColor(context.getResources().getColor(R.color.material_deep_teal_500));
            trailersHeader.setPadding(
                    convertPixeltoDP(context, 16),
                    convertPixeltoDP(context, 16),
                    convertPixeltoDP(context, 16),
                    convertPixeltoDP(context, 16));
            trailersHeader.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            rootLinearLayout.addView(trailersHeader);
            for (Trailer trailer: trailers)
                addTrailerToView(context, rootLinearLayout, trailer);
            DetailActivityFragment.mShareActionProvider.setShareIntent(DetailActivityFragment.createShareForecastIntent());
        }

        if (reviews.size() > 0) {
            TextView reviewssHeader = new TextView(context);
            reviewssHeader.setText("Reviews");
            reviewssHeader.setTextAppearance(context, R.style.trailerHeader);
            reviewssHeader.setBackgroundColor(context.getResources().getColor(R.color.material_deep_teal_500));
            reviewssHeader.setPadding(
                    convertPixeltoDP(context, 16),
                    convertPixeltoDP(context, 16),
                    convertPixeltoDP(context, 16),
                    convertPixeltoDP(context, 16));
            reviewssHeader.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            rootLinearLayout.addView(reviewssHeader);
            for (Review review: reviews)
                addReviewToView(context, rootLinearLayout, review);
        }
    }



    // Method to check if any movies are in the database.  Used to prevent runtime errors.
    public static boolean existsAnyFavoriteMovie(Context context) {
        mdbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = mdbHelper.getReadableDatabase();

        try {
            String count = "SELECT * FROM " + MovieContract.FavMoviesEntry.TABLE_NAME;
            Cursor c = db.rawQuery(count, null);

            try {

                if (c != null && c.getCount() > 0) {
                    c.close();
                    return true;
                } else {
                    c.close();
                    return false;
                }
            } finally {
                // Close the cursor in case it is still open.
                c.close();
            }
        } finally {
            db.close();
        }
    }

    // Method to check if the movie is already in the database
    // Returns true if the movie, by ID, is there.
    // From http://stackoverflow.com/questions/20415309/android-sqlite-how-to-check-if-a-record-exists
    public static boolean checkMovieInDB(Context context, String movieID) {
        mdbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = mdbHelper.getReadableDatabase();
        try {
            String Query = "Select * from " + MovieContract.FavMoviesEntry.TABLE_NAME
                    + " where " + MovieContract.FavMoviesEntry.COLUMN_MOVIE_ID + " = " + movieID;
            Cursor cursor = db.rawQuery(Query, null);
            try {
                if (cursor.getCount() <= 0) {
                    cursor.close();
                    return false;
                }
            } finally {
                // Close the cursor
                cursor.close();
            }
        } finally {
            //Close the database.
            db.close();
        }
        return true;
    }

    public static String addMovieToFavorites(
            Context context, Movie movie, ArrayList<Trailer> trailers, ArrayList<Review> reviews){


            mdbHelper = new MovieDbHelper(context);
            SQLiteDatabase db = mdbHelper.getWritableDatabase();

            View rootView = ((Activity) context).getWindow().getDecorView().findViewById(R.id.movie_details);
            ImageView image = (ImageView) rootView.findViewById(R.id.movie_poster);
            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
            String savedImagePath = saveImage(context, bitmap, movie.getId());
        long newRowId;

        try {

            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.FavMoviesEntry.COLUMN_MOVIE_ID, movie.getId());
            movieValues.put(MovieContract.FavMoviesEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
            movieValues.put(MovieContract.FavMoviesEntry.COLUMN_MOVIE_RELEASE_DATE, movie.getRelease());
            movieValues.put(MovieContract.FavMoviesEntry.COLUMN_MOVIE_RUNTIME, movie.getRuntime());
            movieValues.put(MovieContract.FavMoviesEntry.COLUMN_MOVIE_VOTE_AVERAGE, movie.getRating());
            movieValues.put(MovieContract.FavMoviesEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
            movieValues.put(MovieContract.FavMoviesEntry.COLUMN_MOVIE_POSTER_PATH, movie.getPosterPath());
            movieValues.put(MovieContract.FavMoviesEntry.COLUMN_MOVIE_LOCAL_POSTER_PATH, savedImagePath);
            movieValues.put(MovieContract.FavMoviesEntry.COLUMN_MOVIE_BACKDROP_PATH, movie.getBackdropPath());


            newRowId = db.insert(
                    MovieContract.FavMoviesEntry.TABLE_NAME,
                    null,
                    movieValues);

            for (int i = 0; i < reviews.size(); i++) {
                Review thisReview = reviews.get(i);
                ContentValues reviewValues = new ContentValues();
                reviewValues.put(MovieContract.ReviewsEntry.COLUMN_MOVIE_KEY, movie.getId());
                reviewValues.put(MovieContract.ReviewsEntry.COLUMN_REVIEW_ID, thisReview.getId());
                reviewValues.put(MovieContract.ReviewsEntry.COLUMN_REVIEW_AUTHOR, thisReview.getAuthor());
                reviewValues.put(MovieContract.ReviewsEntry.COLUMN_REVIEW_CONTENT, thisReview.getContent());
                reviewValues.put(MovieContract.ReviewsEntry.COLUMN_REVIEW_URL, thisReview.getUrl());

                //Insert each review into the table.
                db.insert(MovieContract.ReviewsEntry.TABLE_NAME, null, reviewValues);
            }

            for (int i = 0; i < trailers.size(); i++) {
                Trailer thisTrailer = trailers.get(i);
                ContentValues reviewValues = new ContentValues();
                reviewValues.put(MovieContract.TrailersEntry.COLUMN_MOVIE_KEY, movie.getId());
                reviewValues.put(MovieContract.TrailersEntry.COLUMN_TRAILER_NAME, thisTrailer.getName());
                reviewValues.put(MovieContract.TrailersEntry.COLUMN_TRAILER_SIZE, thisTrailer.getSize());
                reviewValues.put(MovieContract.TrailersEntry.COLUMN_TRAILER_SOURCE, thisTrailer.getSource());
                reviewValues.put(MovieContract.TrailersEntry.COLUMN_TRAILER_TYPE, thisTrailer.getType());

                //Insert each trailer into the table.
                db.insert(MovieContract.TrailersEntry.TABLE_NAME, null, reviewValues);
            }
        } finally {
            // Close the database.
            db.close();
        }

        return savedImagePath;
    }

    /* Remove a movie from favorites list.
     *
     */

    public static boolean deleteMovieFromFavorites(Context context, String movieID) {

        mdbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = mdbHelper.getWritableDatabase();

        try {
            db.delete(
                    MovieContract.ReviewsEntry.TABLE_NAME,
                    MovieContract.ReviewsEntry.COLUMN_MOVIE_KEY + "=" + movieID,
                    null);
            db.delete(
                    MovieContract.TrailersEntry.TABLE_NAME,
                    MovieContract.TrailersEntry.COLUMN_MOVIE_KEY + "=" + movieID,
                    null);
            db.delete(
                    MovieContract.FavMoviesEntry.TABLE_NAME,
                    MovieContract.FavMoviesEntry.COLUMN_MOVIE_ID + "=" + movieID,
                    null);
            return true;
        } catch (Exception e){
            return false;
        } finally {
            db.close();
        }
    }

    public static Movie getMovieFromDB(Context context, String movie) {

        mdbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = mdbHelper.getReadableDatabase();

        Movie returnMovie;

        try {
            String query = "Select * from " + MovieContract.FavMoviesEntry.TABLE_NAME
                    + " where " + MovieContract.FavMoviesEntry.COLUMN_MOVIE_ID + " = " + movie;

            Cursor cursor = db.rawQuery(query, null);
            try {
                cursor.moveToFirst();
                long movieID = cursor.getLong(
                        cursor.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_ID)
                );
                returnMovie = new Movie(
                        cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_RELEASE_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_RUNTIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_VOTE_AVERAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_OVERVIEW)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_POSTER_PATH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_LOCAL_POSTER_PATH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_BACKDROP_PATH))
                );
            } finally {
                // Close the cursor.
                cursor.close();
            }
        } finally {
            // Close the database.
            db.close();
        }

        return returnMovie;
    }

    /* Method to query the database for a list of favorite movies
     *
     *
     */
    public static ArrayList<Movie> getFavorites(Context context) {

        // Setup an arraylist to hold the movies as they are queried from database.
        ArrayList<Movie> returnedMovies = new ArrayList<>();
        // Establish a connection to the database.
        mdbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = mdbHelper.getReadableDatabase();

        try {
            // Setup query to pull all data for favorite movies.
            String query = "SELECT * FROM " + MovieContract.FavMoviesEntry.TABLE_NAME;
            Cursor c = db.rawQuery(query, null);

            try {


                // Set cursor at beginning.
                c.moveToFirst();

                // Iterate through the rows of the database, adding a new Movie object to the array
                // and move the cursor to the next row.
                for (int i = 0; i < c.getCount(); i++) {
                    returnedMovies.add(i, new Movie(
                            c.getString(c.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_ID)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_TITLE)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_RELEASE_DATE)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_RUNTIME)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_VOTE_AVERAGE)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_OVERVIEW)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_POSTER_PATH)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_LOCAL_POSTER_PATH)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.FavMoviesEntry.COLUMN_MOVIE_BACKDROP_PATH))
                    ));
                    c.moveToNext();
                }
            } finally {
                // Close the cursor.
                c.close();
            }

        } finally {
            // Close the database.
            db.close();
        }

        // Return the Movie ArrayList so that it may be used to populate the gridview.
        return returnedMovies;
    }

    public static ArrayList<Trailer> getTrailerArray(Context context, Movie movie) {

        ArrayList<Trailer> returnedTrailers = new ArrayList<>();

        mdbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = mdbHelper.getReadableDatabase();

        try {
            // Setup query to pull all data for favorite movies.
            String query = "SELECT * FROM " + MovieContract.TrailersEntry.TABLE_NAME + " WHERE " +
                    MovieContract.TrailersEntry.COLUMN_MOVIE_KEY + " = " + movie.getId();
            Cursor c = db.rawQuery(query, null);

            try {
                c.moveToFirst();

                // Iterate through the rows of the database, adding a new Movie object to the array
                // and move the cursor to the next row.
                for (int i = 0; i < c.getCount(); i++) {
                    returnedTrailers.add(i, new Trailer(
                            c.getString(c.getColumnIndexOrThrow(MovieContract.TrailersEntry.COLUMN_TRAILER_NAME)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.TrailersEntry.COLUMN_TRAILER_SIZE)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.TrailersEntry.COLUMN_TRAILER_SOURCE)),
                            c.getString(c.getColumnIndexOrThrow(MovieContract.TrailersEntry.COLUMN_TRAILER_TYPE))
                    ));
                    c.moveToNext();
                }
            } finally {
                // Close the cursor.
                c.close();
            }
        } finally {
            // Close the database.
            db.close();
        }

        // Return the Movie ArrayList so that it may be used to populate the gridview.
        return returnedTrailers;
    }

    public static ArrayList<Review> getReviewArray(Context context, Movie movie) {

        ArrayList<Review> returnedReviews = new ArrayList<>();

        mdbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = mdbHelper.getReadableDatabase();

        // Setup query to pull all data for favorite movies.
        String query = "SELECT * FROM " + MovieContract.ReviewsEntry.TABLE_NAME + " WHERE " +
                MovieContract.ReviewsEntry.COLUMN_MOVIE_KEY + " = " + movie.getId();
        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();

        // Iterate through the rows of the database, adding a new Movie object to the array
        // and move the cursor to the next row.
        for (int i = 0; i < c.getCount(); i++) {
            returnedReviews.add(i, new Review(
                    c.getString(c.getColumnIndexOrThrow(MovieContract.ReviewsEntry.COLUMN_REVIEW_ID)),
                    c.getString(c.getColumnIndexOrThrow(MovieContract.ReviewsEntry.COLUMN_REVIEW_AUTHOR)),
                    c.getString(c.getColumnIndexOrThrow(MovieContract.ReviewsEntry.COLUMN_REVIEW_CONTENT)),
                    c.getString(c.getColumnIndexOrThrow(MovieContract.ReviewsEntry.COLUMN_REVIEW_URL))
            ));
            c.moveToNext();
        }

        // Close the cursor and database.
        c.close();
        db.close();

        // Return the Movie ArrayList so that it may be used to populate the gridview.
        return returnedReviews;
    }

    // Method to store the movie poster into local image directory for use in favorite movies
    // gridview even if the phone is offline.
    private static String saveImage(Context context, Bitmap bitmapImage, String fileName){
        ContextWrapper cw = new ContextWrapper(context);

        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        // Create imageDir
        File imageDir = new File(directory, fileName);

        FileOutputStream fileOutputStream = null;
        try {

            fileOutputStream = new FileOutputStream(imageDir);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath() + "/" + fileName;
    }

    private static void addTrailerToView(final Context context, LinearLayout rootLinearLayout, final Trailer trailer) {

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        int padding16dp = convertPixeltoDP(context, 16);
        int padding8dp = convertPixeltoDP(context, 8);
        int padding4dp = convertPixeltoDP(context, 4);

        LinearLayout.LayoutParams subLinearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        linearLayout.setLayoutParams(subLinearParams);

        TextView nameTextView = new TextView(context);
        nameTextView.setText(trailer.getName());
        nameTextView.setTextSize(16);
        nameTextView.setTextColor(Color.BLACK);
        nameTextView.setPadding(padding16dp, padding8dp, padding16dp, 0);
        nameTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView infoTextView = new TextView(context);
        infoTextView.setText(trailer.getType() + " - " + trailer.getSize());
        infoTextView.setPadding(padding16dp, 0, padding16dp, padding8dp);
        infoTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView spacerTextView = new TextView(context);
        spacerTextView.setBackgroundColor(Color.GRAY);
        spacerTextView.setHeight(1);
        spacerTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Loading video...", Toast.LENGTH_SHORT).show();
                String videoKEY = trailer.getSource();
                try {
                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("vnd.youtube:" + videoKEY));
                    intent.putExtra("VIDEO_ID", videoKEY);
                    context.startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + videoKEY));
                    context.startActivity(intent);
                }
            }
        });
        linearLayout.setClickable(true);
        linearLayout.addView(nameTextView);
        linearLayout.addView(infoTextView);
        linearLayout.addView(spacerTextView);
        rootLinearLayout.addView(linearLayout);

    }

    private static void addReviewToView(final Context context, LinearLayout rootLinearLayout, final Review review) {

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        int padding16dp = convertPixeltoDP(context, 16);
        int padding8dp = convertPixeltoDP(context, 8);
        int padding4dp = convertPixeltoDP(context, 4);

        LinearLayout.LayoutParams subLinearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        linearLayout.setLayoutParams(subLinearParams);

        TextView nameTextView = new TextView(context);
        nameTextView.setText(review.getContent());
        nameTextView.setTextColor(Color.BLACK);
        nameTextView.setPadding(padding16dp, padding8dp, padding16dp, 0);
        nameTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView authorTextView = new TextView(context);
        authorTextView.setTextAppearance(context, R.style.reviewAuthor);
        authorTextView.setText("- " + review.getAuthor());
        authorTextView.setPadding(padding16dp, 0, padding16dp, padding8dp);
        authorTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView spacerTextView = new TextView(context);
        spacerTextView.setBackgroundColor(Color.GRAY);
        spacerTextView.setHeight(1);
        spacerTextView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        //Future - set a link on review.
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Loading review...", Toast.LENGTH_SHORT).show();
                String url = review.getUrl();
                try {
                    Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "You cannot open the link.", Toast.LENGTH_LONG).show();
                }
            }
        });
        linearLayout.setClickable(true);
        linearLayout.addView(nameTextView);
        linearLayout.addView(authorTextView);
        linearLayout.addView(spacerTextView);
        rootLinearLayout.addView(linearLayout);

    }

    private static int convertPixeltoDP(Context context, int pixels) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pixels * scale + 0.5f);
    }


}
