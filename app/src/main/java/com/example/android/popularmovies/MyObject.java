package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;


// MyObject class implements Parcelable in order to use onSaveInstanceState to restore app state
// and movie data after device rotation without downloading data again
public class MyObject implements Parcelable {

    // MyObject contains a string for each movie data item
    String title;
    String poster;
    String synopsis;
    String rating;
    String release;
    String id;

    public MyObject(String title, String poster, String overview,
                    String vote, String release, String id) {
        this.title = title;
        this.poster = poster;
        this.synopsis = overview;
        this.rating = vote;
        this.release = release;
        this.id = id;
    }

    private MyObject(Parcel in) {
        title = in.readString();
        poster = in.readString();
        synopsis = in.readString();
        rating = in.readString();
        release = in.readString();
        id = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public String getTitle() {
        return this.title;
    }

    public String getPoster() {
        return this.poster;
    }

    public String getSynopsis() {
        return this.synopsis;
    }

    public String getRating() {
        return this.rating;
    }

    public String getRelease() {
        return this.release;
    }

    public String getId() {
        return this.id;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(poster);
        out.writeString(synopsis);
        out.writeString(rating);
        out.writeString(release);
        out.writeString(id);
    }

    public static final Parcelable.Creator<MyObject> CREATOR = new Parcelable.Creator<MyObject>() {
        public MyObject createFromParcel(Parcel in) {
            return new MyObject(in);
        }

        public MyObject[] newArray(int size) {
            return new MyObject[size];
        }
    };
}

