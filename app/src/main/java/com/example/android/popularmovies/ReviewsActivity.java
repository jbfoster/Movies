package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.Serializable;

/**
 * Created by User on 5/7/2016.
 * This activity displays reviews for a selected movie
 */
public class ReviewsActivity extends AppCompatActivity {

    public Serializable reviewsArray;
    public String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // pull reviews and movie title data from intent
        Intent intent = this.getIntent();
        if (intent != null && (intent.getExtras() != null)) {
            title = intent.getStringExtra("title");
            reviewsArray = intent.getSerializableExtra("reviewData");
        }

        ReviewsFragment fragment = new ReviewsFragment();
        setContentView(R.layout.activity_reviews);
        Bundle arguments = new Bundle();
        arguments.putSerializable("reviewsData", reviewsArray);
        arguments.putString("title", title);
        fragment.setArguments(arguments);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }
    }
}
