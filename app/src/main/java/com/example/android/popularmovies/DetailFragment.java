package com.example.android.popularmovies;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

// DetailFragment displays the information for a selected movie
public class DetailFragment extends Fragment {

    public View rootView; // global variable to store base linear layout view
    public String[] movieStr; // array to hold movie data
    public String[] trailersInfo; // array to store trailers data

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Intent intent = getActivity().getIntent();
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Bundle bundle = getArguments(); // pull arguments sent from DetailActivity
        movieStr = bundle.getStringArray("movieData");
        trailersInfo = bundle.getStringArray("trailersData");

        // Pull data sent with intent and assign the appropriate data values
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
        displayTrailers();
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