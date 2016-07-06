package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements MainActivityFragment.Callback, DetailFragment.Callback {

    private static final String DETAILFRAGMENT_TAG = "DFTAG"; // tag used for onResume
    private static final String REVIEWFRAGMENT_TAG = "RFRAG"; // tag used for onResume
    private boolean mTwoPane; // tells whether device is in one pane or two pane mode
    DetailFragment fragment = new DetailFragment(); // used to call button methods in fragments


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_container) != null) { // device is in two pane mode
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu that includes Settings
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handle menu item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // SettingsActivity class handles menu selections from Settings menu
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onMovieSelected(String[] movieStr) {
        if (mTwoPane) {
            DetailFragment fragment = new DetailFragment();
            Bundle args = new Bundle();
            args.putSerializable("movieData", movieStr);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG).commit();

        } else {
            Intent detailIntent = new Intent(this, DetailActivity.class).putExtra("movieData", movieStr);
            startActivity(detailIntent);
        }
    }

    // method onShowReviews will only be called from this activity when in two pane mode
    // this method should replace the detailfragment with the reviews fragment
    public void onShowReviews(String[][] reviewsStr, String title) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putSerializable("reviewsData", reviewsStr);
        args.putString("title", title);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment, REVIEWFRAGMENT_TAG).commit();
    }
}
