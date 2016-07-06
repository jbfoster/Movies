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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

// DetailActivy is executed when a movie poster is clicked to display information for
// the specific movie that was selected
public class DetailActivity extends AppCompatActivity implements DetailFragment.Callback {

    public String[] movieStr; // global variable to store movie data to pass to Review Activity
    DetailFragment fragment = new DetailFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = this.getIntent();
        if (intent != null && (intent.getExtras() != null)) {
            movieStr = intent.getStringArrayExtra("movieData");
        }
        startDetailFragment();
    }

/*
    // showReviews method is called when Read Reviews button is clicked
    // ReviewsActivity Intent is started to download and display reviews
    public void showReviews(View view) {
        fragment.showReviews(view);
    }
*/

    // addFavorite method is called when Add to Favorites button is clicked
    public void addFavorite(View view) {
        fragment.addFavorite(view);
    }

    public void onShowReviews(String[][] reviewsStr, String title) {
        Intent reviewsIntent = new Intent(this, ReviewsActivity.class);
        reviewsIntent.putExtra("title", title); // send movie title
        Bundle bundle = new Bundle();
        bundle.putSerializable("reviewData", reviewsStr);
        reviewsIntent.putExtras(bundle); // send review data
        startActivity(reviewsIntent);
    }

    // startDetailFragment starts the new fragment and passes movie, trailers and reviews data
    private void startDetailFragment() {
        setContentView(R.layout.activity_detail);
        Bundle arguments = new Bundle();
        arguments.putStringArray("movieData", movieStr); // send movie data to Fragment
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.movie_detail_container, fragment).commit();
    }
}
