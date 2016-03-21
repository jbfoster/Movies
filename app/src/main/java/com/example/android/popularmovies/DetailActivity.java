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
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

// DetailActivy is executed when a movie poster is clicked to display information for
// the specific movie that was selected
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    // DetailFragment displays the information for a selected movie
    public static class DetailFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // Pull data sent with intent and assign the appropriate data values
            if (intent != null && (intent.getExtras() != null)) {
                String[] movieStr = intent.getStringArrayExtra("data");
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
            }
            return rootView;
        }
    }
}