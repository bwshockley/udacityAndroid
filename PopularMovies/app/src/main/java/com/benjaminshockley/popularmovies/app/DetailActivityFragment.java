package com.benjaminshockley.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * This is the Detail Activity Fragment that will create the Movie Details view,
 * fetch the movie details from themoviedb.com via the movie ID, read the data,
 * and finally update the UI with the movie details.
 *
 * This activity is called from an intent from the MovieListFragment activity with
 * the movie ID passed in the intent.
 */
public class DetailActivityFragment extends Fragment {

    private String mID,mTitle,mPosterPath,mDate,mRuntime,mRating,mOverview,tempDate,mBackdropPath;

    //This is the Intent extra that contains the movie data.
    private Movie mMovie;

    private String movieLocalPosterPath;

    public static ShareActionProvider mShareActionProvider;

    // ArrayLists for the trailers and the reviews of the movie.
    private static ArrayList<Trailer> mTrailerArrayList;
    private static ArrayList<Review> mReviewArrayList;

    // Views associated with the movie that will be updated with this fragment.
    public static TextView runtimeView;
    public static LinearLayout infoLinearLayout;


    public DetailActivityFragment() {
    }

    public boolean checkConnection(){
        // Check if there is a network connection and set the isConnected boolean.
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected;

        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("movie")) {
            //The detail Activity called via intent or arguments.
            // Inspect the data, choose the right one for the right layout.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                Bundle data = intent.getExtras();
                mMovie = (Movie) data.getParcelable(Intent.EXTRA_TEXT);
            } else if (getArguments() != null) {
                Movie myMovie = getArguments().getParcelable("movie");
                if (myMovie != null) {
                    mMovie = myMovie;
                }
            }

        } else {
            mMovie = savedInstanceState.getParcelable("movie");
            mTrailerArrayList = savedInstanceState.getParcelableArrayList("trailers");
            mReviewArrayList = savedInstanceState.getParcelableArrayList("reviews");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Add additional menu items.  Here we are adding our sort-by items.

        inflater.inflate(R.menu.menu_detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    public static Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "http://www.youtube.com/watch?v=" + mTrailerArrayList.get(0).getSource());
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        runtimeView = (TextView) rootView.findViewById(R.id.movie_length);
        infoLinearLayout = (LinearLayout) rootView.findViewById(R.id.info_container);


        if (savedInstanceState == null || !savedInstanceState.containsKey("movie")) {

            if (Utility.checkMovieInDB(getActivity(), mMovie.getId())) {

                // SavedInstanceState does not exist, but the movie is a favorite, therefore
                // data can be pulled from the database.
                mMovie = Utility.getMovieFromDB(getActivity(), mMovie.getId());
                mTrailerArrayList = Utility.getTrailerArray(getActivity(), mMovie);
                mReviewArrayList = Utility.getReviewArray(getActivity(), mMovie);
                Utility.populateDetails(
                        getActivity(),
                        infoLinearLayout,
                        mMovie,
                        mReviewArrayList,
                        mTrailerArrayList);
            } else {

                // SavedInstanceState does not exist, and the movie is not a favorite, therefore
                // data must be pulled from the network if the connection is available.
                if (checkConnection()) {
                    FetchMovieDetailsTask fetchDetails = new FetchMovieDetailsTask();
                    fetchDetails.execute(mMovie.getId());
                } else {
                    Toast.makeText(getActivity(), MovieListFragment.NO_CONNECTION, Toast.LENGTH_LONG).show();
                    //return rootView;
                }
            }

        } else {
            // If a savedInstanceState exists, such as screen rotation, repopulate details.

            if (Utility.checkMovieInDB(getActivity(), mMovie.getId())) {
                //Log.v("onCreateView", "Instance Exists, no Connection, favorite");

                // SavedInstanceState does not exist, but the movie is a favorite, therefore
                // data can be pulled from the database.
                mMovie = Utility.getMovieFromDB(getActivity(), mMovie.getId());
                mTrailerArrayList = Utility.getTrailerArray(getActivity(), mMovie);
                mReviewArrayList = Utility.getReviewArray(getActivity(), mMovie);
                Utility.populateDetails(
                        getActivity(),
                        infoLinearLayout,
                        mMovie,
                        mReviewArrayList,
                        mTrailerArrayList);
            } else if(mTrailerArrayList != null) {
                //Log.v("onCreateView", "Instance Exists with info, no Connection, not favorite");
                Utility.populateDetails(
                        getActivity(),
                        infoLinearLayout,
                        mMovie,
                        mReviewArrayList,
                        mTrailerArrayList);
            } else {
                //Log.v("onCreateView", "Instance Exists but not with info, no Connection, not favorite");
            }
        }

        mID = mMovie.getId();
        mTitle = mMovie.getTitle();
        tempDate = mMovie.getRelease();
        if (tempDate.equals("")) {
            mDate = "No Release Date";
        } else {
            mDate = tempDate.substring(0, 4);  //Keep only the year.
        }
        mPosterPath = mMovie.getPosterPath();
        mBackdropPath = mMovie.getBackdropPath();
        mRating = mMovie.getRating() + "/10.0";
        mOverview = mMovie.getOverview();

        TextView titleView = (TextView) rootView.findViewById(R.id.movie_title);
        titleView.setText(mTitle);
        ImageView posterView = (ImageView) rootView.findViewById(R.id.movie_poster);
        //ImageView backdropView = (ImageView) getActivity().findViewById(R.id.movie_backdrop);
        //Picasso.with(getActivity()).load(mBackdropPath).into(backdropView);
        TextView releaseView = (TextView) rootView.findViewById(R.id.movie_year);
        releaseView.setText(mDate);
        TextView ratingView = (TextView) rootView.findViewById(R.id.movie_rating);
        ratingView.setText(mRating);
        TextView overviewView = (TextView) rootView.findViewById(R.id.movie_description);
        overviewView.setText(mOverview);


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Always call the superclass to save the view hierarchy state.
        super.onSaveInstanceState(outState);
        outState.putParcelable("movie", mMovie);
        outState.putParcelableArrayList("trailers", mTrailerArrayList);
        outState.putParcelableArrayList("reviews", mReviewArrayList);
    }

    @Override
    public void onStart() {
        super.onStart();

        final Button favButton = (Button) getActivity().findViewById(R.id.button_favorite);
        if (Utility.checkMovieInDB(getActivity(), mMovie.getId())) {
            favButton.setText(R.string.remove_from_favorites);
        } else if (!Utility.checkMovieInDB(getActivity(), mMovie.getId()) && !checkConnection()) {
            favButton.setVisibility(View.GONE);
        }

        // Set the favorite button click listener.
        favButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //If the movie is not in the database already, add it and change the UI to reflect.
                if (!Utility.checkMovieInDB(getActivity(), mMovie.getId()) && checkConnection()) {
                    movieLocalPosterPath = Utility.addMovieToFavorites(
                            getActivity(), mMovie, mTrailerArrayList, mReviewArrayList);
                    mMovie.setLocalPosterPath(movieLocalPosterPath);
                    Toast.makeText(getActivity(),
                            mMovie.getTitle() + " " + getString(R.string.movie_added),
                            Toast.LENGTH_SHORT).show();
                    favButton.setText(R.string.remove_from_favorites);
                }  else {
                    // If it is already there, you can remove it.
                    boolean result = Utility.deleteMovieFromFavorites(getActivity(), mMovie.getId());
                    Toast.makeText(getActivity(),
                            mMovie.getTitle() + " " + getString(R.string.movie_removed),
                            Toast.LENGTH_SHORT).show();
                    if (checkConnection()) {
                        favButton.setText(R.string.add_to_favorite);
                    } else {
                        favButton.setVisibility(View.GONE);
                    }
                }
            }
        });

        // Load the poster from the network if the movie is not in the favorites list.
        // If it is in the favorites database, load the local poster.
        ImageView posterView = (ImageView) getActivity().findViewById(R.id.movie_poster);
        if (!Utility.checkMovieInDB(getActivity(), mMovie.getId())) {
            //Use Picasso to load the image from the url into each imageView.
            Picasso.with(getContext()).load(mPosterPath).into(posterView);
        } else {
            try {
                if (mMovie.getLocalPosterPath() != null) {
                    File fip = new File(mMovie.getLocalPosterPath());
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(fip));
                    posterView.setImageBitmap(b);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public class FetchMovieDetailsTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();

        private Void getMovieDetailsFromJson(String detailsJsonStr) throws JSONException {


            // This is the location of the video detail data within the JSON data.
            final String MOVIE_ID = "id";
            final String DET_RUNTIME = "runtime";
            final String DET_TRAILERS = "trailers";
            final String TRL_SITE = "youtube";
            final String DET_REVIEWS = "reviews";
            final String RVW_RESULTS = "results";

            //Strings for the details contained in the trailers and reviews arrays.
            final String YT_NAME = "name";
            final String YT_SIZE = "size";
            final String YT_SOURCE = "source";
            final String YT_TYPE = "type";
            final String RVW_ID = "id";
            final String RVW_AUTHOR = "author";
            final String RVW_CONTENT = "content";
            final String RVW_URL = "url";


            // Convert the string into a JSON object.
            JSONObject detailsJson = new JSONObject(detailsJsonStr);

            // First, let's set the movie detail runtime - as this isn't provided on our initial
            // movie list request.
            mMovie.setRuntime(detailsJson.getString(DET_RUNTIME));

            // Select the trailers and reviews information from the JSON data.
            JSONObject trailersJSON = detailsJson.getJSONObject(DET_TRAILERS);
            JSONObject reviewsObjJSON = detailsJson.getJSONObject(DET_REVIEWS);
            JSONArray youtubeJSON = trailersJSON.getJSONArray(TRL_SITE);
            JSONArray reviewsJSON = reviewsObjJSON.getJSONArray(RVW_RESULTS);


            mTrailerArrayList = new ArrayList<>();
            mReviewArrayList = new ArrayList<>();

            //Create the parcelable objects array with the trailer details.
            for (int i = 0; i < youtubeJSON.length(); i++) {
                // Get video details for each result.
                JSONObject trailerDetailsObject = youtubeJSON.getJSONObject(i);
                mTrailerArrayList.add(i, new Trailer(
                        trailerDetailsObject.getString(YT_NAME),
                        trailerDetailsObject.getString(YT_SIZE),
                        trailerDetailsObject.getString(YT_SOURCE),
                        trailerDetailsObject.getString(YT_TYPE)
                ));
            }

            //Create the parcelable objects array with the review details.
            for (int i = 0; i < reviewsJSON.length(); i++) {
                // Get video details for each result.
                JSONObject trailerDetailsObject = reviewsJSON.getJSONObject(i);
                mReviewArrayList.add(i, new Review(
                        trailerDetailsObject.getString(RVW_ID),
                        trailerDetailsObject.getString(RVW_AUTHOR),
                        trailerDetailsObject.getString(RVW_CONTENT),
                        trailerDetailsObject.getString(RVW_URL)
                        ));
            }

            //Return nothing.
            return null;
        }


        @Override
        protected String[] doInBackground(String... params) {

            //If No string is passed:
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String videoJsonStr = null;
            //Replace API Key with your own API Key - Do not share API Key.
            String api_key = "";

            try

            {
                // Construct the URL for the Movie List
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_PARAM = "api_key";
                final String APPEND_PARAM = "append_to_response";
                final String APPEND_TO_RESPONSE = "trailers,reviews";

                Uri detailsUri;

                // This default param isn't necessary for this specific project, however it could
                // be used in the future should we choose a different default set of movies.
                // If the parameter passed is default, fetch the movies without the sort_by option.
                detailsUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendQueryParameter(API_PARAM, api_key)
                        .appendQueryParameter(APPEND_PARAM, APPEND_TO_RESPONSE)
                        .build();

                URL url = new URL(detailsUri.toString());

                // Create the request to TheMovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                videoJsonStr = buffer.toString();

                //Log.v("Details JSON: ", videoJsonStr);

            } catch (
                    IOException e
                    )

            {
                Log.e("Error ", e.toString());
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } finally

            {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, e.toString());
                    }
                }
            }

            try {
                getMovieDetailsFromJson(videoJsonStr);
            } catch (JSONException e) {
                Log.e("Details Fetch Error", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(final String[] strings) {

            Utility.populateDetails(
                    getActivity(),
                    infoLinearLayout,
                    mMovie,
                    mReviewArrayList,
                    mTrailerArrayList);

        }

    }
}
