package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by User on 5/7/2016.
 * This activity displays reviews for a selected movie
 */
public class ReviewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ReviewsFragment())
                    .commit();
        }
    }

    // ReviewsFragment displays reviews for a selected movie
    public static class ReviewsFragment extends Fragment {

        public View rootView; // define rootView for use by methods
        public String[][] reviewsInfo; // array to store review data

        @Override
        public void onStart() {
            super.onStart();
            //getReviews();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // pull reviews data sent with intent
            Intent intent = getActivity().getIntent();
            rootView = inflater.inflate(R.layout.fragment_reviews, container, false);
            if (intent != null && (intent.getExtras() != null)) {
                String title = intent.getStringExtra("title");

                // get 2d array from intent and store in reviewsInfo array
                Object[] objectArray = (Object[]) intent.getExtras().getSerializable("reviewData");
                if (objectArray != null) {
                    reviewsInfo = new String[objectArray.length][];
                    for (int i = 0; i < objectArray.length; i++) {
                        reviewsInfo[i] = (String[]) objectArray[i];
                    }
                }

                ((TextView) rootView.findViewById(R.id.title_text)).setText(title);
            }
            displayReviews();
            return rootView;
        }

        public void displayReviews() {
            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.review_layout);

            // cycle through each review and add text views for author and review
            for (String[] review : reviewsInfo) {
                String author = "Review by " + review[0] + ":";
                TextView authorText = new TextView(getActivity());
                authorText.setText(author);
                authorText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                authorText.setPadding(0, 0, 0, 10);
                TextView reviewText = new TextView(getActivity());
                reviewText.setText(review[1]);
                reviewText.setPadding(0, 0, 0, 20);
                linearLayout.addView(authorText);
                linearLayout.addView(reviewText);

            }
        }

        // getReviews method gets reviews by executing FetchReviewsTask
        private void getReviews() {
            // FetchReviewsTask gets reviews from The Movie Database
            FetchReviewsTask reviewsTask = new FetchReviewsTask();
            if (Utility.isNetworkAvailable(getActivity())) { // only download reviews if network is available
                reviewsTask.execute();
            }
        }

        // FetchReviewsTask class contains methods for getting reviews from The Movie Database
        public class FetchReviewsTask extends AsyncTask<String, Void, String[]> {
            private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

            /**
             * Take the String representing reviews in JSON Format and
             * parse data.
             */
            private String[] getReviewsFromJson(String reviewsJsonStr)
                    throws JSONException {

                // These are the names of the JSON objects that need to be extracted.
                final String MDB_RESULTS = "results";
                final String MDB_AUTHOR = "author";
                final String MDB_REVIEW = "content";

                JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
                JSONArray reviewsArray = reviewsJson.getJSONArray(MDB_RESULTS);

                // Create array to hold reviews
                reviewsInfo = new String[reviewsArray.length()][2];

                String[] resultStrs = new String[reviewsArray.length()];
                for (int i = 0; i < reviewsArray.length(); i++) {

                    // Get the JSON object representing the ith review
                    JSONObject movieData = reviewsArray.getJSONObject(i);

                    // Store the data for each review in reviewsInfo array
                    reviewsInfo[i][0] = movieData.getString(MDB_AUTHOR);
                    reviewsInfo[i][1] = movieData.getString(MDB_REVIEW);

                    resultStrs[i] = reviewsInfo[i][1];
                }

                return resultStrs;
            }


            // doInBackground executes the API call in a background thread
            @Override
            protected String[] doInBackground(String... params) {
                String movieId = null;

                Intent intent = getActivity().getIntent();
                if (intent != null && (intent.getExtras() != null)) {
                    String[] movieStr = intent.getStringArrayExtra("movieData");
                    movieId = movieStr[5];
                }

                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String reviewsJsonStr = null;

                try {

                    // Construct the URL for the The Movie Database query
                    // Information and possible parameters are available at themoviedb.org
                    final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/movie/";
                    final String API_KEY = "api_key";
                    // api key for The Movie Database is stored in api-keys.xml resource file
                    final String KEY = getString(R.string.movie_api_key);

                    String urlString = FORECAST_BASE_URL + movieId + "/" + "reviews";

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
                    reviewsJsonStr = buffer.toString();

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
                    return getReviewsFromJson(reviewsJsonStr);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }

                return null;
            }

            // Once background thread is completed, add text views for each review
            @Override
            protected void onPostExecute(String[] result) {
                if (result != null) {
                    LinearLayout linearLayout = (LinearLayout)
                            rootView.findViewById(R.id.review_layout);

                    // cycle through each review and add text views for author and review
                    for (String[] review : reviewsInfo) {
                        String author = "Review by " + review[0] + ":";
                        TextView authorText = new TextView(getActivity());
                        authorText.setText(author);
                        authorText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                        authorText.setPadding(0, 0, 0, 10);
                        TextView reviewText = new TextView(getActivity());
                        reviewText.setText(review[1]);
                        reviewText.setPadding(0, 0, 0, 20);
                        linearLayout.addView(authorText);
                        linearLayout.addView(reviewText);

                    }

                }
            }
        }
    }


}
