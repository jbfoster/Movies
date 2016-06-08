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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// DetailActivy is executed when a movie poster is clicked to display information for
// the specific movie that was selected
public class DetailActivity extends AppCompatActivity {

    String[] movieStr; // global variable to store movie data to pass to Review Activity
    public String[] trailersInfo; // array to store trailers data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        if (intent != null && (intent.getExtras() != null)) {
            movieStr = intent.getStringArrayExtra("data");
        }
        getTrailers();
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

                if (trailersInfo != null) { // add trailers to database if they exist
                    ContentValues trailerValues = new ContentValues();
                    for (String trailer : trailersInfo) {
                        trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_LINK, trailer);
                        trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, movieRowId);
                        resolver.insert(MovieContract.TrailerEntry.CONTENT_URI, trailerValues);
                    }
                }


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
        Cursor movieCursor = resolver.query(MovieContract.TrailerEntry.CONTENT_URI,
                null, null, null, null);
        if (movieCursor.moveToFirst()) {
            do {
                String word = movieCursor.getString(movieCursor.getColumnIndex(
                        MovieContract.TrailerEntry.COLUMN_TRAILER_LINK));
                String id = movieCursor.getString(movieCursor.getColumnIndex(
                        MovieContract.TrailerEntry.COLUMN_MOVIE_KEY));
                Toast toast = Toast.makeText(getApplication(), word, Toast.LENGTH_SHORT);
                toast.show();
                Toast toast2 = Toast.makeText(getApplication(), id, Toast.LENGTH_SHORT);
                toast2.show();
            } while (movieCursor.moveToNext());
        }
        movieCursor.close();
    }

    // getTrailers method gets trailers by executing FetchTrailersTask
    private void getTrailers() {
        // FetchTrailersTask gets trailers from The Movie Database
        FetchTrailersTask trailersTask = new FetchTrailersTask();
        if (Utility.isNetworkAvailable(this)) { // only download reviews if network is available
            trailersTask.execute();
        }
    }

    // FetchTrailersTask class contains methods for getting trailers from The Movie Database
    public class FetchTrailersTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        /**
         * Take the String representing trailers in JSON Format and parse data
         */
        private String[] getTrailersFromJson(String trailersJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MDB_RESULTS = "results";
            final String MDB_LINK = "key";

            JSONObject trailersJson = new JSONObject(trailersJsonStr);
            JSONArray trailersArray = trailersJson.getJSONArray(MDB_RESULTS);

            // Create array to hold reviews
            trailersInfo = new String[trailersArray.length()];

            String[] resultStrs = new String[trailersArray.length()];
            for (int i = 0; i < trailersArray.length(); i++) {

                // Get the JSON object representing the ith trailer
                JSONObject movieData = trailersArray.getJSONObject(i);

                // Store the data for each trailer in trailersInfo array
                trailersInfo[i] = movieData.getString(MDB_LINK);

                resultStrs[i] = trailersInfo[i];
            }

            return resultStrs;
        }


        // doInBackground executes the API call in a background thread
        @Override
        protected String[] doInBackground(String... params) {
            String movieId = movieStr[5];

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailersJsonStr = null;

            try {

                // Construct the URL for the The Movie Database query
                // Information and possible parameters are available at themoviedb.org
                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String API_KEY = "api_key";
                // api key for The Movie Database is stored in api-keys.xml resource file
                final String KEY = getString(R.string.movie_api_key);

                String urlString = FORECAST_BASE_URL + movieId + "/" + "videos";

                Uri builtUri = Uri.parse(urlString).buildUpon()
                        .appendQueryParameter(API_KEY, KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to The Movie Database, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                trailersJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in
                // attempting to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailersFromJson(trailersJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        // Once background thread is completed, add text views for each trailer
        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                setContentView(R.layout.activity_detail);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new DetailFragment())
                        .commit();
            }
        }
    }
}