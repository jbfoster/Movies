package com.example.android.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

/**
 * Created by User on 5/15/2016.
 */
public class TestUtilities extends AndroidTestCase {

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static ContentValues createToyStoryValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Toy Story");
        testValues.put(MovieContract.MovieEntry.COLUMN_POSTER_IMAGE, "toy story poster");
        testValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, "A movie about toys");
        testValues.put(MovieContract.MovieEntry.COLUMN_RATING, 4.1);
        testValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_YEAR, 1999);

        return testValues;
    }

    static ContentValues createReviewValues(long locationRowId) {
        ContentValues reviewValues = new ContentValues();
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, locationRowId);
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, "Roger Ebert");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_TEXT, "This was a good movie");

        return reviewValues;
    }

    static ContentValues createTrailerValues(long locationRowId) {
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_KEY, locationRowId);
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_LINK, "Toy Story trailer");

        return trailerValues;
    }


    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

}
