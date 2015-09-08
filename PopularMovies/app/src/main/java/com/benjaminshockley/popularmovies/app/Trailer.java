package com.benjaminshockley.popularmovies.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by benjaminshockley on 8/30/15.
 * This parcelable class might be beneficial in the future to pass through an intent.
 * For now it shall be used to create trailer objects.
 */
public class Trailer implements Parcelable {
    private String name;
    private String size;
    private String source;
    private String type;

    //Constructor
    public Trailer(String name,
                   String size,
                   String source,
                   String type)
    {
        this.name = name;
        this.size = size;
        this.source = source;
        this.type = type;
    }

    //Parcelling Section
    public Trailer(Parcel in){
        String[] trailerData = new String[4];

        in.readStringArray(trailerData);
        this.name = trailerData[0];
        this.size = trailerData[1];
        this.source = trailerData[2];
        this.type = trailerData[3];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    //Getter and Setter Methods
    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                this.name,
                this.size,
                this.source,
                this.type
        });
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };
}
