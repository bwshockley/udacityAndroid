package com.benjaminshockley.popularmovies.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by benjaminshockley on 9/4/15.
 * This Fragment only populates an empty fragment as a placeholder.
 * Use when no movie details can be, or should be shown.
 */
public class PlaceholderFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.placeholder_detail, container, false);
    }
}
