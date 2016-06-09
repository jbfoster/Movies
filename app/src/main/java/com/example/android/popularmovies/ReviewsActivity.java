package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    }
}
