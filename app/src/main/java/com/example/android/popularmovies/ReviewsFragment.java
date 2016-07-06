package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by User on 7/4/2016.
 * This fragment displays movie reviews
 */

// ReviewsFragment displays reviews for a selected movie
public class ReviewsFragment extends Fragment {

    public View rootView; // define rootView for use by methods
    public String[][] reviewsInfo; // array to store review data
    public String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments(); // pull arguments sent from DetailActivity
        Object[] objectArray = (Object[]) bundle.getSerializable("reviewsData");
        if (objectArray != null) {
            reviewsInfo = new String[objectArray.length][];
            for (int i = 0; i < objectArray.length; i++) {
                reviewsInfo[i] = (String[]) objectArray[i];
            }
            title = bundle.getString("title");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_reviews, container, false);
        ((TextView) rootView.findViewById(R.id.title_text)).setText(title);
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
