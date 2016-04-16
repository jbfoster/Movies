package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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
import java.util.ArrayList;


// Fragment for getting movie data.

public class MainActivityFragment extends Fragment {

    // create a 2-D array to hold movie information downloaded from Movie Database
    public String[][] movieInfo;

    // create a gridview as a global variable in order to access movie data
    public GridView gridView;

    // create a flag that is set when there is saved movie data from savedInstance
    // if this flag is true, onCreateView can display images immediately
    // if this flag is false, images are displayed after background fetchmovies task completes
    public boolean savedData = false;

    public MainActivityFragment() {
    }

    // Load movie data from savedInstanceState if it exists
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            ArrayList<MyObject> list;
            list = savedInstanceState.getParcelableArrayList("key");

            movieInfo = new String[20][5];

            for (int i = 0; i < 20; i++) {
                movieInfo[i][0] = list.get(i).getTitle();
                movieInfo[i][1] = list.get(i).getPoster();
                movieInfo[i][2] = list.get(i).getSynopsis();
                movieInfo[i][3] = list.get(i).getRating();
                movieInfo[i][4] = list.get(i).getRelease();
            }
            savedData = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!savedData) { // download movie data if no savedinstancestate
            getMovies();
        }

        if (savedData) { // reset savedData flag and display movies using stored data
            savedData = false;
            displayImages();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create a gridview for grid of movie posters
        gridView = (GridView) rootView.findViewById(R.id.gridview);

        // When a movie poster is clicked, start DetailActivity intent
        // passing in the array of movie data for the selected movie
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getActivity(),
                        DetailActivity.class).putExtra("data", movieInfo[position]);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    // Saved moviedata as Bundle so data doesn't have to be downloaded again
    // after device rotation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (movieInfo != null) {
            ArrayList<MyObject> list = new ArrayList<MyObject>();
            for (int i = 0; i < 20; i++) { // cycle through list of 20 movies on screen
                list.add(new MyObject(movieInfo[i][0], movieInfo[i][1],
                        movieInfo[i][2], movieInfo[i][3], movieInfo[i][4]));
            }
            outState.putParcelableArrayList("key", list);
            super.onSaveInstanceState(outState);
        }
    }


    // ImageAdapter class is custom adapater for gridview
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return 20; // 20 movies are returned from API call
        }

        public Object getItem(int position) {
            return null; // this method is not needed so return null
        }

        public long getItemId(int position) {
            return 0; // this method is not needed so return 0
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER);

                // set AdjustViewBounds to true in order to scale poster images
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (ImageView) convertView;
            }

            String url = "http://image.tmdb.org/t/p/w185/";
            url = url + movieInfo[position][1];
            Picasso.with(getActivity()).load(url).into(imageView);

            return imageView;
        }
    }

    // getMovies method gets movie data by executing FetchMoviesTask
    private void getMovies() {
        // FetchMoviesTask gets movie data from The Movie Database
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        if (isNetworkAvailable()) { // only download movies if network is available
            moviesTask.execute();
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

    // displayImages method displays poster images using picasso by setting imageadapter
    public void displayImages() {
        gridView.setAdapter(new ImageAdapter(getActivity()));
    }


    // FetchMoviesTask class contains methods for getting movie data from The Movie Database
    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MDB_RESULTS = "results";
            final String MDB_TITLE = "original_title";
            final String MDB_POSTER = "poster_path";
            final String MDB_SYNOPSIS = "overview";
            final String MDB_RATING = "vote_average";
            final String MDB_RELEASE = "release_date";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(MDB_RESULTS);

            // Create array to hold relevant data for each movie
            movieInfo = new String[moviesArray.length()][5];

            String[] resultStrs = new String[moviesArray.length()];
            for (int i = 0; i < moviesArray.length(); i++) {

                // Get the JSON object representing the ith movie
                JSONObject movieData = moviesArray.getJSONObject(i);

                // Store the data for each movie in movieInfo array
                movieInfo[i][0] = movieData.getString(MDB_TITLE);
                movieInfo[i][1] = movieData.getString(MDB_POSTER);
                movieInfo[i][2] = movieData.getString(MDB_SYNOPSIS);
                movieInfo[i][3] = movieData.getString(MDB_RATING);
                movieInfo[i][4] = movieData.getString(MDB_RELEASE);

                resultStrs[i] = movieInfo[i][1];
            }

            return resultStrs;
        }


        // doInBackground executes the API call in a background thread
        @Override
        protected String[] doInBackground(String... params) {


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {

                // Construct the URL for the The Movie Database query
                // Information and possible parameters are available at themoviedb.org
                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String API_KEY = "api_key";
                // api key for The Movie Database is stored in api-keys.xml resource file
                final String KEY = getString(R.string.movie_api_key);

                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                String orderType = sharedPrefs.getString(
                        getString(R.string.pref_order_key),
                        getString(R.string.pref_order_popular));

                String urlString = FORECAST_BASE_URL + orderType + "/";

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
                moviesJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
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
                return getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        // Once background thread is completed, onPostExecute uses Picasso to set
        // the images of each movie poster
        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                displayImages();
            }
        }
    }

}


