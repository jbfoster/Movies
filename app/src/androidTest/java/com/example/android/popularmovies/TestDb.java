package com.example.android.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.data.MovieDbHelper;

/**
 * Created by User on 5/14/2016.
 */
public class TestDb extends AndroidTestCase {

    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
    }

    public long insertMovie() {
        // insert row into database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createToyStoryValues();
        long locationRowID;
        locationRowID = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);
        assertTrue(locationRowID != -1);

        // query database
        Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                null, null, null, null, null, null);

        assertTrue("Error: no records returned", cursor.moveToFirst());
        TestUtilities.validateCurrentRecord("Error: data not valid", cursor, testValues);
        assertFalse("Error: more than one record returned", cursor.moveToNext());

        cursor.close();
        db.close();
        return locationRowID;

    }

    public void testMovieTable() {
        insertMovie();
    }

    public void testReviewTable() {
        long locationRowId = insertMovie();
        assertFalse("Error: movie not inserted", locationRowId == -1);

        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues reviewValues = TestUtilities.createReviewValues(locationRowId);
        long reviewRowId = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, reviewValues);
        assertTrue("Error: review not inserted", reviewRowId != -1);
        Cursor reviewCursor = db.query(MovieContract.ReviewEntry.TABLE_NAME,
                null, null, null, null, null, null);

        assertTrue("Error: no review records returned", reviewCursor.moveToFirst());
        TestUtilities.validateCurrentRecord("reviewEntry failed to validate",
                reviewCursor, reviewValues);

        assertFalse("Error: more than one review record returned", reviewCursor.moveToNext());

        reviewCursor.close();
        dbHelper.close();

    }

    public void testTrailerTable() {
        long locationRowId = insertMovie();
        assertFalse("Error: movie not inserted", locationRowId == -1);

        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues trailerValues = TestUtilities.createTrailerValues(locationRowId);
        long trailerRowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, trailerValues);
        assertTrue("Error: trailer not inserted", trailerRowId != -1);
        Cursor trailerCursor = db.query(MovieContract.TrailerEntry.TABLE_NAME,
                null, null, null, null, null, null);

        assertTrue("Error: no trailer records returned", trailerCursor.moveToFirst());
        TestUtilities.validateCurrentRecord("reviewEntry failed to validate",
                trailerCursor, trailerValues);

        assertFalse("Error: more than one trailer record returned", trailerCursor.moveToNext());

        trailerCursor.close();
        dbHelper.close();

    }

}