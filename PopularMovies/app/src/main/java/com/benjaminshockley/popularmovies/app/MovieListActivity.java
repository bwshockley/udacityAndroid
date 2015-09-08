package com.benjaminshockley.popularmovies.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MovieListActivity extends AppCompatActivity {

    public static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.fragment_moviedetail) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_moviedetail, new PlaceholderFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_movielist1, new MovieListFragment())
                        .commit();
            }

        }

    }

}
