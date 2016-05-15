package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;


// TrailersObject class implements Parcelable in order to use onSaveInstanceState to restore
// trailer data after device rotation without downloading data again
public class TrailersObject implements Parcelable {

    // MyObject contains a string for each movie data item
    String trailerLink;

    public TrailersObject(String trailerLink) {
        this.trailerLink = trailerLink;
    }

    private TrailersObject(Parcel in) {
        trailerLink = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getTrailerLink() {
        return this.trailerLink;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(trailerLink);
    }

    public static final Parcelable.Creator<TrailersObject> CREATOR = new Parcelable.Creator<TrailersObject>() {
        public TrailersObject createFromParcel(Parcel in) {
            return new TrailersObject(in);
        }

        public TrailersObject[] newArray(int size) {
            return new TrailersObject[size];
        }
    };
}

