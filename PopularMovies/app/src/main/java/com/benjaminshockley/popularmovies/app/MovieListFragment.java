package com.benjaminshockley.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * This is the main fragment that is called when the application starts.  This
 * fragment starts by setting up views and menus, then pulling initial movie data
 * from the API for "default".  Default is set as for no sort_by (which happens to be the
 * same as sort_by popularity).  Then the movie data is parsed as a JSONObject and for
 * each movie poster a gridview item (ImageView) is created.
 */
public class MovieListFragment extends Fragment {

    //Setup mGridView as GridView.
    private GridView mGridView;

    public static boolean lastInFavoriteView;

    public static final String DETAILFRAGMENT_TAG = "DFTAG";

    private ImageAdapter movieListAdapter;

    //This will be the array list of movies pulled from the JSON data.
    private ArrayList<Movie> movieArrayList;

    //Display error message if no network connection is present.
    public static String NO_CONNECTION = "No Network Connection Detected. " +
            "Please connect to the internet and try again.";

    public MovieListFragment() {
    }

    public boolean checkConnection() {
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
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            movieArrayList = new ArrayList<>();
        } else {
            movieArrayList = savedInstanceState.getParcelableArrayList("movies");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("movies", movieArrayList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Add additional menu items.  Here we are adding our sort-by items.
        inflater.inflate(R.menu.menu_movielistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        switch (item.getItemId()) {
            case R.id.sort_popular:
                if (checkConnection()) {
                    movieArrayList.clear();
                    lastInFavoriteView = false;
                    FetchMovieListTask PopMovieTask = new FetchMovieListTask();
                    PopMovieTask.execute("popular");
                } else {
                    Toast.makeText(getActivity(), NO_CONNECTION, Toast.LENGTH_LONG).show();
                    return true;
                }
                return true;
            case R.id.sort_rating:
                if (checkConnection()) {
                    movieArrayList.clear();
                    lastInFavoriteView = false;
                    FetchMovieListTask RatingMovieTask = new FetchMovieListTask();
                    RatingMovieTask.execute("rating");
                } else {
                    Toast.makeText(getActivity(), NO_CONNECTION, Toast.LENGTH_LONG).show();
                    return true;
                }
                return true;
            case R.id.sort_favorites:
                if (Utility.existsAnyFavoriteMovie(getActivity())) {
                    movieArrayList.clear();
                    lastInFavoriteView = true;
                    movieArrayList = Utility.getFavorites(getActivity());
                    populateGridView(getActivity(), mGridView, movieListAdapter, movieArrayList);
                } else {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_moviedetail, new PlaceholderFragment())
                            .commit();
                    Toast.makeText(getActivity(), "You don't have any favorites yet.", Toast.LENGTH_LONG).show();
                    if (checkConnection()) {
                        movieArrayList.clear();
                        lastInFavoriteView = false;
                        FetchMovieListTask PopMovieTask = new FetchMovieListTask();
                        PopMovieTask.execute("popular");
                    }
                    return true;
                }
                return true;
            case R.id.action_settings:
                //Settings Menu.
                //TODO: Do we need a settings menu?  Not yet at least, but we'll save its place.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Setup savedInstanceSate
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            //Set the initial GridView and populate it with default movies.
            mGridView = (GridView) rootView.findViewById(R.id.gridview_movies);
            FetchMovieListTask PopMovieTask = new FetchMovieListTask();
            if (checkConnection()) {
                lastInFavoriteView = false;
                PopMovieTask.execute("default");
            } else {
                Toast.makeText(getActivity(), NO_CONNECTION, Toast.LENGTH_LONG).show();
            }
        } else {
            mGridView = (GridView) rootView.findViewById(R.id.gridview_movies);
            populateGridView(getActivity(), mGridView, movieListAdapter, movieArrayList);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check if the previous gridView showed favorites.  If so
        if (lastInFavoriteView) {
            movieArrayList.clear();
            movieArrayList = Utility.getFavorites(getActivity());
            populateGridView(getActivity(), mGridView, movieListAdapter, movieArrayList);
        }
        lastInFavoriteView = false;
    }

    // Method to build the Gridview with content via the ImageAdapter and set the intents.
    // This is used throughout the app when a new list of movies is required.
    public void populateGridView(
            final Context context,
            GridView grid,
            ImageAdapter imageAdapter,
            final ArrayList<Movie> arrayList) {

        // Instruct the Gridview Adapter to add the movie posters.
        imageAdapter = new ImageAdapter(context, arrayList);
        grid.setAdapter(imageAdapter);

        // Launch intent to open DetailActivity for specific movie upon click.
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                if (MovieListActivity.mTwoPane) {

                    Bundle args = new Bundle();
                    args.putParcelable("movie", arrayList.get(position));

                    DetailActivityFragment fragment = new DetailActivityFragment();
                    fragment.setArguments(args);

                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_moviedetail, fragment, DETAILFRAGMENT_TAG)
                            .commit();
                } else {
                    Intent intent = new Intent(context, DetailActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, arrayList.get(position));
                    context.startActivity(intent);
                }
            }
        });
    }

    public class FetchMovieListTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchMovieListTask.class.getSimpleName();

        private String[] getMovieDetailsFromJson(String movieJsonStr) throws JSONException {

            // May Use this is as a UI variable in the future.  Such that, if the device
            // is a tablet, grab larger images.
            // TODO: Code for determining device size and setting imageSize accordingly.
            String imageSize = "w342";
            String backdropSize = "w500";

            // This is the location of the movie detail data within the JSON data.
            final String MDB_RESULTS = "results";
            final String MDB_ID = "id";
            final String MDB_TITLE = "original_title";
            final String MDB_POSTER_PATH = "poster_path";
            final String MDB_BACKDROP_PATH = "backdrop_path";
            final String MDB_RELEASE_DATE = "release_date";
            final String MDB_RATING = "vote_average";
            final String MDB_OVERVIEW = "overview";
            final String MDB_BASE_PATH = "http://image.tmdb.org/t/p/";

            // Convert the string into a JSON object - then pull the results Array out.
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULTS);

            //Log.v("movieArray:",movieArray.toString());

            // Create an array to hold the poster URL paths.
            String[] posterPathStrings = new String[movieArray.length()];

            //Create the parcelable object with the movie details.
            for (int i = 0; i < movieArray.length(); i++) {

                // Get Movie details for each result.
                JSONObject movieDetailsObject = movieArray.getJSONObject(i);

                //Add the URL to our array of poster images.
                posterPathStrings[i] = MDB_BASE_PATH + imageSize + movieDetailsObject.getString(MDB_POSTER_PATH);

                if (Utility.checkMovieInDB(getActivity(), movieDetailsObject.getString(MDB_ID))) {
                    movieArrayList.add(i, Utility.getMovieFromDB(getActivity(), movieDetailsObject.getString(MDB_ID)));
                } else {

                    movieArrayList.add(i, new Movie(
                            movieDetailsObject.getString(MDB_ID),
                            movieDetailsObject.getString(MDB_TITLE),
                            movieDetailsObject.getString(MDB_RELEASE_DATE),
                            null,
                            movieDetailsObject.getString(MDB_RATING),
                            movieDetailsObject.getString(MDB_OVERVIEW),
                            posterPathStrings[i],
                            null,
                            MDB_BASE_PATH + backdropSize + movieDetailsObject.getString(MDB_BACKDROP_PATH)
                    ));
                }
            }

            //Return the posterPathStrings so that they may be used in the GridView ImageAdapter
            return posterPathStrings;
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
            String movieJsonStr = null;
            //Replace API Key with your own API Key - Do not share API Key.
            String api_key = "";

            try

            {
                // Construct the URL for the Movie List
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie";

                // The min_vote_count string sets a minimum vote count a movie must have
                // in order to be considered for the rating selection.  In the future, this
                // could be a user setting.
                final String MIN_VOTE_COUNT ="1000";
                final String VOTE_QUERY_PARAM = "vote_count.gte";
                final String SORT_QUERY_PARAM = "sort_by";
                final String API_PARAM = "api_key";
                String SORT_BY = "";

                if (params[0] == "popular")
                    SORT_BY = "popularity.desc";

                if (params[0] == "rating")
                    SORT_BY = "vote_average.desc";

                Uri builtUri;

                // This default param isn't necessary for this specific project, however it could
                // be used in the future should we choose a different default set of movies.
                // If the parameter passed is default, fetch the movies without the sort_by option.
                // Setting "default" will be the same as not setting any param.

                switch (params[0]) {
                    case "default":
                        builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                                .appendQueryParameter(API_PARAM, api_key)
                                .build();
                        break;
                    case "popular":
                        builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                                .appendQueryParameter(SORT_QUERY_PARAM, SORT_BY)
                                .appendQueryParameter(API_PARAM, api_key)
                                .build();
                        break;
                    case "rating":
                        builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                                .appendQueryParameter(VOTE_QUERY_PARAM, MIN_VOTE_COUNT)
                                .appendQueryParameter(SORT_QUERY_PARAM, SORT_BY)
                                .appendQueryParameter(API_PARAM, api_key)
                                .build();
                        break;
                    default:
                        builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                                .appendQueryParameter(API_PARAM, api_key)
                                .build();
                        break;
                }

                URL url = new URL(builtUri.toString());

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
                movieJsonStr = buffer.toString();

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
                return getMovieDetailsFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e("Poster ULR Fetch Error", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(final String[] strings) {

            // Populate the gridview with the movie posters using the movieArrayList, ListAdapter
            // and the gridview itself.
            populateGridView(getActivity(), mGridView, movieListAdapter, movieArrayList);
        }
    }
}
