package com.benjaminshockley.popularmovies.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * This is my ImageAdapter that extends the BaseAdapter  - See GridView Android for where
 * most of this information came from.  I started there and added the String[] strings param
 * so that I can bring into the adapter my list of movie poster urls.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;

    private ArrayList<Movie> mMovieList;

    private boolean mFavorite = false;

    //Set my constructor.  Bring in context and the array of movie poster URLs.
    public ImageAdapter(Context c, ArrayList<Movie> movieList) {
        mContext = c;
        mMovieList = movieList;
    }

    public int getCount() {
        //Check if we happen to have data - if not, don't try and set views.
        if (mMovieList == null) {
            return 0;
        }
        else {
            return mMovieList.size();
        }
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter and load it
    // with the image retrieved by Picasso.
    public View getView(int position, View convertView, ViewGroup parent) {
        mFavorite = Utility.checkMovieInDB(mContext, mMovieList.get(position).getId());
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (ImageView) convertView;
        }

        if (!mFavorite) {
            //Use Picasso to load the image from the url into each imageView.
            Picasso.with(mContext).load(mMovieList.get(position).getPosterPath()).into(imageView);
        } else if (mMovieList.get(position).getLocalPosterPath() != null){
            try {
                File fip = new File(mMovieList.get(position).getLocalPosterPath());
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(fip));
                imageView.setImageBitmap(b);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        return imageView;
    }

}
