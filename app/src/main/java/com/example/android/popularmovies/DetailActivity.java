/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.popularmovies;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;

// DetailActivy is executed when a movie poster is clicked to display information for
// the specific movie that was selected
public class DetailActivity extends AppCompatActivity {

    String[] movieStr; // global variable to store movie data to pass to Review Activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        if (intent != null && (intent.getExtras() != null)) {
            movieStr = intent.getStringArrayExtra("data");
        }
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    // getReviews method is called when Read Reviews button is clicked
    // ReviewsActivity Intent is started to download and display reviews
    public void getReviews(View view) {
        Intent reviewsIntent = new Intent(this,
                ReviewsActivity.class).putExtra("data", movieStr);
        startActivity(reviewsIntent);
    }

    // addFavorite method is called when Add to Favorites button is clicked
    public void addFavorite(View view) {
        ContentResolver resolver = getApplication().getContentResolver();
        Cursor movieCursor = resolver.query(MovieContract.MovieEntry.CONTENT_URI,
                null, null, null, null);
        boolean existsInFavorites = false; // flag to set if title is already in favorites
        long movieRowId; // ID of movie to use as foreign key for review and trailer inserts

        if (movieCursor.moveToFirst()) { // check if any entries exist in the database
            do {
                String title = movieCursor.getString(movieCursor.getColumnIndex(
                        MovieContract.MovieEntry.COLUMN_TITLE));

                if (title.equals(movieStr[0])) { // check if title is already in favorites
                    existsInFavorites = true;
                }
            } while (movieCursor.moveToNext()); // cycle through all titles in favorites
        }

            // add the new movie to the database if it is not already included
            if (existsInFavorites) {
                Toast toast = Toast.makeText(getApplication(),
                        "This movie is already in favorites", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                ContentValues movieValues = new ContentValues();
                movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movieStr[0]);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, movieStr[1]);
                movieValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, movieStr[2]);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, movieStr[3]);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, movieStr[4]);
                Uri MovieUri = resolver.insert(MovieContract.MovieEntry.CONTENT_URI, movieValues);
                movieRowId = ContentUris.parseId(MovieUri);

                ContentValues reviewValues = new ContentValues();
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, movieRowId);
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, "Gene Siskel");
                reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_TEXT, "Bad movie");
                resolver.insert(MovieContract.ReviewEntry.CONTENT_URI, reviewValues);
            }
        movieCursor.close();
    }

    public void displayDB(View view) { // method for testing database functionality
        ContentResolver resolver = getApplication().getContentResolver();
        Cursor movieCursor = resolver.query(MovieContract.ReviewEntry.CONTENT_URI,
                null, null, null, null);
        if (movieCursor.moveToFirst()) {
            do {
                String word = movieCursor.getString(movieCursor.getColumnIndex(
                        MovieContract.ReviewEntry.COLUMN_REVIEW_TEXT));
                String id = movieCursor.getString(movieCursor.getColumnIndex(
                        MovieContract.ReviewEntry.COLUMN_MOVIE_KEY));
                Toast toast = Toast.makeText(getApplication(), word, Toast.LENGTH_SHORT);
                toast.show();
                Toast toast2 = Toast.makeText(getApplication(), id, Toast.LENGTH_SHORT);
                toast2.show();
            } while (movieCursor.moveToNext());
        }
        movieCursor.close();
    }
}