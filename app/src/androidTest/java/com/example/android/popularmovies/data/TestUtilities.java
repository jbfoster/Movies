package com.example.android.popularmovies.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.android.popularmovies.utils.PollingCheck;

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

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

}
