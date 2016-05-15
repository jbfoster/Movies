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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

    // getReviews method is called when Read Reviews button is clicked
    // ReviewsActivity Intent is started to download and display reviews
    public void getReviews(View view) {
        Intent reviewsIntent = new Intent(this,
                ReviewsActivity.class).putExtra("data", movieStr);
        startActivity(reviewsIntent);
    }

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

    // DetailFragment displays the information for a selected movie
    public static class DetailFragment extends Fragment {

        public View rootView; // global variable to store base linear layout view
        String[] movieStr; // array to hold movie data
        public String[] trailersInfo; // array to store trailers data

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Intent intent = getActivity().getIntent();
            rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // Pull data sent with intent and assign the appropriate data values
            if (intent != null && (intent.getExtras() != null)) {
                movieStr = intent.getStringArrayExtra("data");
                ((TextView) rootView.findViewById(R.id.title_text)).setText(movieStr[0]);
                ImageView imageView = (ImageView) rootView.findViewById(R.id.poster_image);
                String url = "http://image.tmdb.org/t/p/w185/";
                url = url + movieStr[1];
                Picasso.with(getActivity()).load(url).into(imageView);
                ((TextView) rootView.findViewById(R.id.year_text))
                        .setText(movieStr[4].substring(0, 4));
                ((TextView) rootView.findViewById(R.id.rating_text))
                        .setText(movieStr[3] + "/10");
                ((TextView) rootView.findViewById(R.id.description_text)).setText(movieStr[2]);
            }
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            getTrailers();
        }

        // getTrailers method gets trailers by executing FetchTrailersTask
        private void getTrailers() {
            // FetchTrailersTask gets trailers from The Movie Database
            FetchTrailersTask trailersTask = new FetchTrailersTask();
            if (isNetworkAvailable()) { // only download reviews if network is available
                trailersTask.execute();
            }
        }

        // isNetworkAvailable() checks for network connectivity
        public boolean isNetworkAvailable() {
            ConnectivityManager cm = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
            return false;
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

            // displayTrailers method
            public void displayTrailers() {

                int counter = 1; // tracks the number of trailers

                // rootLinearLayout is base vertical linear layout
                // each trailer will be added as a new horizontal linear layout
                LinearLayout rootLinearLayout = (LinearLayout)
                        rootView.findViewById(R.id.detail_linear_layout);

                int playImageID = 0; // used to dynamically set IDs for imageview
                // add linear layout and views for each available trailer
                for (String trailer : trailersInfo) {
                    LinearLayout llTrailer = new LinearLayout(getActivity());
                    llTrailer.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    llTrailer.setOrientation(LinearLayout.HORIZONTAL);
                    rootLinearLayout.addView(llTrailer);

                    // add ImageView for play image
                    ImageView playImageView = new ImageView(getActivity());
                    playImageView.setImageResource(R.drawable.button_play);
                    int width = 120;
                    int height = 120;
                    LinearLayout.LayoutParams parms =
                            new LinearLayout.LayoutParams(width, height);
                    parms.setMargins(16, 16, 16, 16);
                    playImageView.setLayoutParams(parms);
                    playImageView.setAdjustViewBounds(true);
                    final String link = trailer; // youtube link for trailer

                    // set ClickListener to start youtube intent when trailer is clicked
                    playImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String video_path = "https://www.youtube.com/watch?v=";
                            video_path = video_path + link;
                            Intent intent =
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(video_path));
                            startActivity(intent);
                        }
                    });
                    llTrailer.addView(playImageView);

                    // add TextView for trailer text
                    TextView trailerText = new TextView(getActivity());
                    trailerText.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    String txt = getResources().getString(R.string.trailer_text);
                    txt = txt + " " + Integer.toString(counter);
                    trailerText.setText(txt);
                    trailerText.setGravity(Gravity.CENTER_VERTICAL);
                    llTrailer.addView(trailerText);

                    // Add separation line
                    View lineView = new View(getActivity());
                    lineView.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 2));
                    lineView.setBackgroundColor(Color.parseColor("#333333"));
                    rootLinearLayout.addView(lineView);

                    counter++;
                }

            }

            // Once background thread is completed, add text views for each review
            @Override
            protected void onPostExecute(String[] result) {
                if (result != null) {
                    displayTrailers();
                }
            }
        }

    }
}