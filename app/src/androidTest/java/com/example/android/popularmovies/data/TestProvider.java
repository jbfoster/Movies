package com.example.android.popularmovies.data;

/**
 * Created by User on 5/15/2016.
 */

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.example.android.popularmovies.data.MovieContract.MovieEntry;
import com.example.android.popularmovies.data.MovieContract.ReviewEntry;
import com.example.android.popularmovies.data.MovieContract.TrailerEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from all database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ReviewEntry.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                TrailerEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movie table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                ReviewEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Review table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                TrailerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Trailer table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
       This helper function deletes all records from both database tables using the database
       functions only.  This is designed to be used to reset the state of the database until the
       delete functionality is available in the ContentProvider.
     */
    public void deleteAllRecordsFromDB() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MovieEntry.TABLE_NAME, null, null);
        db.delete(ReviewEntry.TABLE_NAME, null, null);
        db.delete(TrailerEntry.TABLE_NAME, null, null);
        db.close();
    }


    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
         */
    public void testGetType() {
        // content://com.example.android.popularmovies.app/movie/
        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.popularmovies.app/movie
        assertEquals("Error: the MovieEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
                MovieEntry.CONTENT_TYPE, type);

        // content://com.example.android.popularmovies.app/review/
        type = mContext.getContentResolver().getType(ReviewEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.popularmovies.app/review
        assertEquals("Error: the ReviewEntry CONTENT_URI should return ReviewEntry.CONTENT_TYPE",
                ReviewEntry.CONTENT_TYPE, type);

        // content://com.example.android.popularmovies.app/trailer/
        type = mContext.getContentResolver().getType(TrailerEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.popularmovies.app/trailer
        assertEquals("Error: the TrailerEntry CONTENT_URI should return TrailerEntry.CONTENT_TYPE",
                TrailerEntry.CONTENT_TYPE, type);

        String testMovieTitle = "Toy Story";
        // content://com.example.android.popularmovies.app/review/Toy_Story
        type = mContext.getContentResolver().getType(
                ReviewEntry.buildReviewMovie(testMovieTitle));
        // vnd.android.cursor.dir/com.example.android.popularmovies.app/movie
        assertEquals("Error: the ReviewEntry CONTENT_URI with movie should return ReviewEntry.CONTENT_TYPE",
                ReviewEntry.CONTENT_TYPE, type);

        // content://com.example.android.popularmovies.app/trailer/Toy_Story
        type = mContext.getContentResolver().getType(
                TrailerEntry.buildTrailerMovie(testMovieTitle));
        // vnd.android.cursor.item/com.example.android.popularmovies.app/trailer/Toy_Story
        assertEquals("Error: the TrailerEntry CONTENT_URI with movie and date should return TrailerEntry.CONTENT_ITEM_TYPE",
                TrailerEntry.CONTENT_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testBasicMovieQuery() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues movieValues = TestUtilities.createToyStoryValues();
        long movieRowID = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);

        db.close();

        // Test the basic content provider query
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMovieQuery", movieCursor, movieValues);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data..
     */
    public void testBasicReviewQueries() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues movieValues = TestUtilities.createToyStoryValues();
        long movieRowID = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);

        ContentValues reviewValues = TestUtilities.createReviewValues(movieRowID);
        long reviewRowID = db.insert(ReviewEntry.TABLE_NAME, null, reviewValues);
        assertTrue("Unable to insert ReviewEntry into database", reviewRowID != -1);

        db.close();

        // Test the basic content provider query
        Cursor reviewCursor = mContext.getContentResolver().query(
                ReviewEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicReviewQueries, review query", reviewCursor, reviewValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Location Query did not properly set NotificationUri",
                    reviewCursor.getNotificationUri(), ReviewEntry.CONTENT_URI);
        }
    }

    public void testBasicTrailerQueries() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues movieValues = TestUtilities.createToyStoryValues();
        long movieRowID = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);

        ContentValues trailerValues = TestUtilities.createTrailerValues(movieRowID);
        long trailerRowID = db.insert(TrailerEntry.TABLE_NAME, null, trailerValues);
        assertTrue("Unable to insert TrailerEntry into database", trailerRowID != -1);

        db.close();

        // Test the basic content provider query
        Cursor trailerCursor = mContext.getContentResolver().query(
                TrailerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicTrailerQueries, trailer query", trailerCursor, trailerValues);

        // Has the NotificationUri been set correctly? --- we can only test this easily against API
        // level 19 or greater because getNotificationUri was added in API level 19.
        if (Build.VERSION.SDK_INT >= 19) {
            assertEquals("Error: Location Query did not properly set NotificationUri",
                    trailerCursor.getNotificationUri(), TrailerEntry.CONTENT_URI);
        }
    }

    /*
        This test uses the provider to insert and then update the data.
     */
    public void testUpdateMovie() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createToyStoryValues();

        Uri movieUri = mContext.getContentResolver().
                insert(MovieEntry.CONTENT_URI, values);
        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);
        Log.d(LOG_TAG, "New row id: " + movieRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(MovieEntry._ID, movieRowId);
        updatedValues.put(MovieEntry.COLUMN_TITLE, "Home Alone");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor locationCursor = mContext.getContentResolver().query(MovieEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        locationCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                MovieEntry.CONTENT_URI, updatedValues, MovieEntry._ID + "= ?",
                new String[]{Long.toString(movieRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        locationCursor.unregisterContentObserver(tco);
        locationCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,   // projection
                MovieEntry._ID + " = " + movieRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateLocation.  Error validating location entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    // Make sure we can still delete after adding/updating stuff
    //

    /*public void testInsertReadProvider() {
        ContentValues movieValues = TestUtilities.createToyStoryValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, tco);
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, movieValues);

        // Did our content observer get called?
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                cursor, movieValues);

        // Fantastic.  Now that we have a movie, add some reviews and trailers!
        ContentValues reviewValues = TestUtilities.createReviewValues(movieRowId);
        //ContentValues trailerValues = TestUtilities.createTrailerValues(movieRowId);
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(ReviewEntry.CONTENT_URI, true, tco);

        Uri reviewInsertUri = mContext.getContentResolver()
                .insert(ReviewEntry.CONTENT_URI, reviewValues);
        assertTrue(reviewInsertUri != null);

        // Did our content observer get called?
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor reviewCursor = mContext.getContentResolver().query(
                ReviewEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating ReviewEntry insert.",
                reviewCursor, reviewValues);

        // Add the movie values in with the review data so that we can make
        // sure that the join worked and we actually get all the values back
        reviewValues.putAll(movieValues);

        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor1 = resolver.query(MovieEntry.CONTENT_URI, null, null, null, null);
        if (cursor1.moveToFirst()) {
            do {
                String word = cursor1.getString(1);
                String abc = word;
            } while (cursor1.moveToNext());
        }

        // Get the joined Review and Movie data
        reviewCursor = mContext.getContentResolver().query(
                ReviewEntry.buildReviewMovie(TestUtilities.TEST_MOVIE),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Review and Movie Data.",
                reviewCursor, reviewValues);
    }*/

    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
//    public void testDeleteRecords() {
//        testInsertReadProvider();
//
//        // Register a content observer for our location delete.
//        TestUtilities.TestContentObserver locationObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(LocationEntry.CONTENT_URI, true, locationObserver);
//
//        // Register a content observer for our weather delete.
//        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, weatherObserver);
//
//        deleteAllRecordsFromProvider();
//
//        // Students: If either of these fail, you most-likely are not calling the
//        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
//        // delete.  (only if the insertReadProvider is succeeding)
//        locationObserver.waitForNotificationOrFail();
//        weatherObserver.waitForNotificationOrFail();
//
//        mContext.getContentResolver().unregisterContentObserver(locationObserver);
//        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
//    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static ContentValues[] createBulkInsertReviewValues(long locationRowId) {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues reviewValues = new ContentValues();
            reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_KEY, locationRowId);
            reviewValues.put(ReviewEntry.COLUMN_AUTHOR, "Gene Siskel");
            reviewValues.put(ReviewEntry.COLUMN_REVIEW_TEXT, "Review # " + Integer.toString(i));
            returnContentValues[i] = reviewValues;
        }
        return returnContentValues;
    }

    // Student: Uncomment this test after you have completed writing the BulkInsert functionality
    // in your provider.  Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time, so really do implement the
    // BulkInsert ContentProvider function.
//    public void testBulkInsert() {
//        // first, let's create a location value
//        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();
//        Uri locationUri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, testValues);
//        long locationRowId = ContentUris.parseId(locationUri);
//
//        // Verify we got a row back.
//        assertTrue(locationRowId != -1);
//
//        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
//        // the round trip.
//
//        // A cursor is your primary interface to the query results.
//        Cursor cursor = mContext.getContentResolver().query(
//                LocationEntry.CONTENT_URI,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//
//        TestUtilities.validateCursor("testBulkInsert. Error validating LocationEntry.",
//                cursor, testValues);
//
//        // Now we can bulkInsert some weather.  In fact, we only implement BulkInsert for weather
//        // entries.  With ContentProviders, you really only have to implement the features you
//        // use, after all.
//        ContentValues[] bulkInsertContentValues = createBulkInsertWeatherValues(locationRowId);
//
//        // Register a content observer for our bulk insert.
//        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
//        mContext.getContentResolver().registerContentObserver(WeatherEntry.CONTENT_URI, true, weatherObserver);
//
//        int insertCount = mContext.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, bulkInsertContentValues);
//
//        // Students:  If this fails, it means that you most-likely are not calling the
//        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
//        // ContentProvider method.
//        weatherObserver.waitForNotificationOrFail();
//        mContext.getContentResolver().unregisterContentObserver(weatherObserver);
//
//        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);
//
//        // A cursor is your primary interface to the query results.
//        cursor = mContext.getContentResolver().query(
//                WeatherEntry.CONTENT_URI,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                WeatherEntry.COLUMN_DATE + " ASC"  // sort order == by DATE ASCENDING
//        );
//
//        // we should have as many records in the database as we've inserted
//        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);
//
//        // and let's make sure they match the ones we created
//        cursor.moveToFirst();
//        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
//            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating WeatherEntry " + i,
//                    cursor, bulkInsertContentValues[i]);
//        }
//        cursor.close();
//    }
}
