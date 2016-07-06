package com.example.android.popularmovies;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.MovieContract;
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

// DetailFragment displays the information for a selected movie
public class DetailFragment extends Fragment implements View.OnClickListener {

    public View rootView; // global variable to store base linear layout view
    public String[] movieStr; // array to hold movie data
    public String[] trailersInfo; // array to store trailers data
    public String[][] reviewsInfo; // array to store reviews data
    public boolean haveTrailers = false; // flag to set when trailers are downloaded
    public boolean haveReviews = false; // flag to set when reviews are downloaded

    // Callback interface that all activities containing this fragment must implement
    // This interface allows activities to be notified when show reviews is selected
    // reviewsStr is 2D array for reviews and title is movie title
    public interface Callback {
        public void onShowReviews(String[][] reviewsStr, String title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments(); // pull arguments sent from DetailActivity
        movieStr = bundle.getStringArray("movieData");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // set onclick listener for add to favorites and show reviews buttons
        Button favoritesButton = (Button) rootView.findViewById(R.id.favoritesButton);
        Button reviewsButton = (Button) rootView.findViewById(R.id.reviewsButton);
        favoritesButton.setOnClickListener(this);
        reviewsButton.setOnClickListener(this);

        // Assign the appropriate data values received from calling object
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
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getTrailersAndReviews();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.favoritesButton:
                addFavorite(v);
                break;

            case R.id.reviewsButton:
                ((Callback) getActivity()).onShowReviews(reviewsInfo, movieStr[0]);
                break;
        }
    }

    // addFavorite method is called when Add to Favorites button is clicked
    public void addFavorite(View view) {
        ContentResolver resolver = getActivity().getContentResolver();
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
            Toast toast = Toast.makeText(getActivity(),
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

            if (reviewsInfo != null) { // add reviews to database if they exist
                ContentValues reviewValues = new ContentValues();
                for (String[] review : reviewsInfo) {
                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review[0]);
                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_TEXT, review[1]);
                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, movieRowId);
                    resolver.insert(MovieContract.ReviewEntry.CONTENT_URI, reviewValues);
                }
            }

            Toast toast = Toast.makeText(getActivity(),
                    "Added to favorites", Toast.LENGTH_SHORT);
            toast.show();
        }
        movieCursor.close();
    }

    // method movieInDB checks whether a movie title is in the local database
    public boolean movieInDB(String title) {
        ContentResolver resolver = getActivity().getContentResolver();
        String[] mProjection = {MovieContract.MovieEntry.COLUMN_TITLE};
        String mSelection = MovieContract.MovieEntry.COLUMN_TITLE + " = ?";
        String mSelectionArgs[] = {title};
        Cursor movieCursor = resolver.query(MovieContract.MovieEntry.CONTENT_URI,
                mProjection, mSelection, mSelectionArgs, null);
        boolean result = movieCursor.moveToFirst();
        movieCursor.close();
        return result;
    }

    // method loadTrailersAndReviews loads trailers and reviews for title from local database
    public void loadTrailersAndReviews(String title) {
        ContentResolver resolver = getActivity().getContentResolver();

        // load trailers from local DB
        String[] trailerProjection = {MovieContract.TrailerEntry.COLUMN_TRAILER_LINK};
        String trailerSelection = MovieContract.MovieEntry.COLUMN_TITLE + " = ?";
        String trailerSelectionArgs[] = {title};
        Cursor trailerCursor = resolver.query(MovieContract.TrailerEntry.buildTrailerMovie(title),
                trailerProjection, trailerSelection, trailerSelectionArgs, null);
        trailersInfo = new String[trailerCursor.getCount()];
        if (trailerCursor.moveToFirst()) {
            int counter = 0; // counter to keep track of index of trailers
            do {
                trailersInfo[counter] = trailerCursor.getString(trailerCursor.getColumnIndex(
                        MovieContract.TrailerEntry.COLUMN_TRAILER_LINK));
                counter++;
            } while (trailerCursor.moveToNext());
        }
        trailerCursor.close();

        // load reviews from local DB
        String[] reviewProjection = {MovieContract.ReviewEntry.COLUMN_AUTHOR,
                MovieContract.ReviewEntry.COLUMN_REVIEW_TEXT};
        String reviewSelection = MovieContract.MovieEntry.COLUMN_TITLE + " = ?";
        String reviewSelectionArgs[] = {title};
        Cursor reviewCursor = resolver.query(MovieContract.ReviewEntry.buildReviewMovie(title),
                reviewProjection, reviewSelection, reviewSelectionArgs, null);
        reviewsInfo = new String[reviewCursor.getCount()][2];
        if (reviewCursor.moveToFirst()) {
            int counter = 0; // counter to keep track of index of trailers
            do {
                reviewsInfo[counter][0] = reviewCursor.getString(reviewCursor.getColumnIndex(
                        MovieContract.ReviewEntry.COLUMN_AUTHOR));
                reviewsInfo[counter][1] = reviewCursor.getString(reviewCursor.getColumnIndex(
                        MovieContract.ReviewEntry.COLUMN_REVIEW_TEXT));
                counter++;
            } while (reviewCursor.moveToNext());
        }
        reviewCursor.close();
    }

    // method getTrailersAndReviews method gets trailers and reviews from MovieDB by executing
    // FetchTrailersTask and FetchReviewsTask or calls loadTrailersAndReviews to get
    // trailers and reviews from local database
    private void getTrailersAndReviews() {
        if (movieInDB(movieStr[0])) { // pull trailers and reviews from local database
            loadTrailersAndReviews(movieStr[0]);
            displayTrailers();
        } else { // get trailers and reviews from The Movie Database
            FetchTrailersTask trailersTask = new FetchTrailersTask();
            FetchReviewsTask reviewsTask = new FetchReviewsTask();
            if (Utility.isNetworkAvailable(getActivity())) { // only download reviews if network is available
                trailersTask.execute();
                reviewsTask.execute();
            }
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
            String reviewsJsonStr = null;

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

        // Start DetailFragment is all background tasks are complete
        @Override
        protected void onPostExecute(String[] result) {
            haveTrailers = true; // set flag
            if ((result != null) && (haveTrailers == true) && (haveReviews == true)) {
                // start DetailFragment if FetchTrailersTask and FetchReviewsTask are complete
                displayTrailers();
            }
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
            String movieId = movieStr[5];

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

        // Start DetailFragment is all background tasks are complete
        @Override
        protected void onPostExecute(String[] result) {
            haveReviews = true; // set flag
            if ((result != null) && (haveTrailers == true) && (haveReviews == true)) {
                // start DetailFragment if FetchTrailersTask and FetchReviewsTask are complete
                displayTrailers();
            }
        }
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
}