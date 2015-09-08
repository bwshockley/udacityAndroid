package com.benjaminshockley.popularmovies.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {

            Intent intent = this.getIntent();
            Log.v("onCreate", "Intent: " + intent.toString());
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                Bundle data = intent.getExtras();
                Bundle args = new Bundle();
                Log.v("DetailActivity", "data: " + data.toString());
                args.putParcelable("movie", data);

                DetailActivityFragment fragment = new DetailActivityFragment();
                fragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_moviedetail, fragment)
                        .commit();
            }
        }
    }
}
