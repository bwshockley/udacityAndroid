package com.benjaminshockley.popularmovies.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by benjaminshockley on 8/30/15.
 */
public class Review implements Parcelable {
    private String id;
    private String author;
    private String content;
    private String url;

    //Constructor
    public Review(String name,
                   String size,
                   String source,
                   String type)
    {
        this.id = name;
        this.author = size;
        this.content = source;
        this.url = type;
    }

    //Parcelling Section
    public Review(Parcel in){
        String[] reviewData = new String[4];

        in.readStringArray(reviewData);
        this.id = reviewData[0];
        this.author = reviewData[1];
        this.content = reviewData[2];
        this.url = reviewData[3];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    //Getter and Setter Methods

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                this.id,
                this.author,
                this.content,
                this.url
        });
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}
