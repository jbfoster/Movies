package com.example.android.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;


public class TestUriMatcher extends AndroidTestCase {
    private static final String TEST_MOVIE_TITLE = "Toy Story";

    // content://com.example.android.sunshine.app/weather"
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_REVIEW_DIR = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_TRAILER_DIR = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_REVIEW_WITH_MOVIE_DIR = MovieContract.ReviewEntry.buildReviewMovie(TEST_MOVIE_TITLE);
    private static final Uri TEST_TRAILER_WITH_MOVIE_DIR = MovieContract.TrailerEntry.buildTrailerMovie(TEST_MOVIE_TITLE);


    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);
        assertEquals("Error: The REVIEW URI was matched incorrectly.",
                testMatcher.match(TEST_REVIEW_DIR), MovieProvider.MOVIE);
        assertEquals("Error: The TRAILEr URI was matched incorrectly.",
                testMatcher.match(TEST_TRAILER_DIR), MovieProvider.MOVIE);
        assertEquals("Error: The REVIEW WITH MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_REVIEW_WITH_MOVIE_DIR), MovieProvider.REVIEW_WITH_MOVIE);
        assertEquals("Error: The TRAILER WITH MOVIE was matched incorrectly.",
                testMatcher.match(TEST_TRAILER_WITH_MOVIE_DIR), MovieProvider.TRAILER_WITH_MOVIE);
    }
}
