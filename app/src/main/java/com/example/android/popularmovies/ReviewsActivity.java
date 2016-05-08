package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by User on 5/7/2016.
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

    // DetailFragment displays the information for a selected movie
    public static class ReviewsFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

            // Pull data sent with intent and assign the appropriate data values
//            if (intent != null && (intent.getExtras() != null)) {
//                String[] movieStr = intent.getStringArrayExtra("data");
//                ((TextView) rootView.findViewById(R.id.title_text)).setText(movieStr[0]);
//                ImageView imageView = (ImageView) rootView.findViewById(R.id.poster_image);
//                String url = "http://image.tmdb.org/t/p/w185/";
//                url = url + movieStr[1];
//                Picasso.with(getActivity()).load(url).into(imageView);
//                ((TextView) rootView.findViewById(R.id.year_text))
//                        .setText(movieStr[4].substring(0, 4));
//                ((TextView) rootView.findViewById(R.id.rating_text))
//                        .setText(movieStr[3] + "/10");
//                ((TextView) rootView.findViewById(R.id.description_text)).setText(movieStr[2]);
//            }
            return rootView;
        }
    }
}
