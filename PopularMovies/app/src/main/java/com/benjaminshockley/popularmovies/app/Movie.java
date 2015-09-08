package com.benjaminshockley.popularmovies.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Benjamin Shockley on 8/12/15.
 * This Movie class implements Parcelable to create a parcelable object
 * that we can pass, through the intent, from the MovieListFragment
 * into the DetailActivityFragment.  It will contain the details for
 * our movies.
 */
public class Movie implements Parcelable {
    private String id;
    private String title;
    private String releaseYear;
    private String runtime;
    private String rating;
    private String overview;
    private String posterPath;
    private String localPosterPath;
    private String backdropPath;

    //Constructor
    public Movie(String id,
                 String title,
                 String releaseYear,
                 String runtime,
                 String rating,
                 String overview,
                 String posterPath,
                 String localPosterPath,
                 String backdropPath)
    {
        this.id = id;
        this.title = title;
        this.releaseYear = releaseYear;
        this.runtime = runtime;
        this.rating = rating;
        this.overview = overview;
        this.posterPath = posterPath;
        this.localPosterPath = localPosterPath;
        this.backdropPath = backdropPath;
    }

    //Getter and Setter Methods

    //Parcelling Section
    public Movie(Parcel in){
        String[] movieData = new String[9];

        in.readStringArray(movieData);
        this.id = movieData[0];
        this.title = movieData[1];
        this.releaseYear = movieData[2];
        this.runtime = movieData[3];
        this.rating = movieData[4];
        this.overview = movieData[5];
        this.posterPath = movieData[6];
        this.localPosterPath = movieData[7];
        this.backdropPath = movieData[8];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getRelease() {
        return releaseYear;
    }

    public String getRuntime() {
        return runtime;
    }

    public Void setRuntime(String runtime) {
        this.runtime = runtime;
        return null;
    }

    public Void setLocalPosterPath(String localPosterPath) {
        this.localPosterPath = localPosterPath;
        return null;
    }

    public String getRating() {
        return rating;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getLocalPosterPath() { return localPosterPath; }

    public String getBackdropPath() {
        return backdropPath;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                this.id,
                this.title,
                this.releaseYear,
                this.runtime,
                this.rating,
                this.overview,
                this.posterPath,
                this.localPosterPath,
                this.backdropPath
        });
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
