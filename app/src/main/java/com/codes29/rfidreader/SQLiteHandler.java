package com.codes29.rfidreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "SIFA_systems";

    // Login table name
    private static final String TABLE_USER = "user_tb";
    //Image table name
    private static final String IMAGE_TABLE = "tbl_image";


    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_KEY = "key";
    private static final String KEY_FNAME = "fname";
    private static final String KEY_SNAME = "sname";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_UPDATED_AT = "updated_at";
    private static final String KEY_TYPE = "type";

    //Image Table Columns names
    private static final String IMAGE_ID = "image_id";
    private static final String TAG_ID = "tag_id";
    private static final String EXAM_ID = "exam_id";
    private static final String STUDENT_NAME = "student_name";
    private static final String IMAGE_URL = "image_url";


    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        //user table
        String query = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_KEY + " TEXT UNIQUE," + KEY_FNAME + " TEXT," + KEY_SNAME + " TEXT,"
                + KEY_LATITUDE + " REAL," + KEY_LONGITUDE + " REAL," + KEY_LOCATION + " TEXT," + KEY_TYPE + " INTEGER,"
                + KEY_UPDATED_AT + " TEXT" + ")";
        db.execSQL(query);

        //image table
        String query2 = "CREATE TABLE " + IMAGE_TABLE + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + IMAGE_ID + " INTEGER," + TAG_ID + " TEXT,"
                + EXAM_ID + " INTEGER," + STUDENT_NAME + " TEXT,"
                + IMAGE_URL + " TEXT" + ")";
        db.execSQL(query2);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + IMAGE_TABLE);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     */
    public void addUser(String key, String fname, String sname, double latitude, double longitude, String location, int type, String updated_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_KEY, key);
        values.put(KEY_FNAME, fname);
        values.put(KEY_SNAME, sname);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put(KEY_LOCATION, location);
        values.put(KEY_TYPE, type);
        values.put(KEY_UPDATED_AT, updated_at);

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    public void addUserImage(int image_id, String tag_id, int exam_id, String student_name, String image_url) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(IMAGE_ID, image_id);
        values.put(TAG_ID, tag_id);
        values.put(EXAM_ID, exam_id);
        values.put(STUDENT_NAME, student_name);
        values.put(IMAGE_URL, image_url);

        // Inserting Row
        long id = db.insert(IMAGE_TABLE, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New Image inserted into DataBase: " + id);

    }


//	public void updateUser(User user, int send) {
//		SQLiteDatabase db = this.getWritableDatabase();
//
//		ContentValues values = new ContentValues();
//		values.put(KEY_SEND, send); // Send
//
//		// Inserting Row
//		long id = db.update(TABLE_USER, values, "name=? and phone=?", new String[] {contact.name, contact.number});
//		db.close(); // Closing database connection
//
//		Log.d(TAG, "New user inserted into sqlite: " + id);
//	}

    //Get Image data
    public ArrayList<UserImage> getImages(String tag_id) {
        ArrayList<UserImage> userImages = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + IMAGE_TABLE + " WHERE " + TAG_ID + " = " + "'"+tag_id+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            UserImage userImage = new UserImage();
            userImage.imageID = cursor.getInt(1);
            userImage.tagID = cursor.getString(2);
            userImage.examID = cursor.getInt(3);
            userImage.studentName = cursor.getString(4);
            userImage.imageURL = cursor.getString(5);

            userImages.add(userImage);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return userImages;
    }


    /**
     * Getting user data from database
     */
    public ArrayList<User> getCoordinators(String string) {

        ArrayList<User> users = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER + " where type=0 and (key LIKE '%" + string + "%' or fname LIKE '%" + string + "%' or sname LIKE '%" + string + "%')";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            User user = new User();
            user.nId = cursor.getInt(0);
            user.key = cursor.getString(1);
            user.fname = cursor.getString(2);
            user.sname = cursor.getString(3);
            user.latitude = cursor.getFloat(4);
            user.longitude = cursor.getFloat(5);
            user.location = cursor.getString(6);
            user.type = cursor.getInt(7);
            user.updated_at = cursor.getString(8);

            users.add(user);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return users;
    }

    public ArrayList<User> getInvigilators(String string) {

        ArrayList<User> users = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER + " where type=1 and key LIKE '%" + string + "%'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            User user = new User();
            user.nId = cursor.getInt(0);
            user.key = cursor.getString(1);
            user.fname = cursor.getString(2);
            user.sname = cursor.getString(3);
            user.latitude = cursor.getFloat(4);
            user.longitude = cursor.getFloat(5);
            user.location = cursor.getString(6);
            user.type = cursor.getInt(7);
            user.updated_at = cursor.getString(8);

            users.add(user);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return users;
    }

    public ArrayList<User> getInvigilator(String string) {

        ArrayList<User> users = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER + " where type=1 and key LIKE '%" + string + "%'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            User user = new User();
            user.nId = cursor.getInt(0);
            user.key = cursor.getString(1);
            user.fname = cursor.getString(2);
            user.sname = cursor.getString(3);
            user.latitude = cursor.getFloat(4);
            user.longitude = cursor.getFloat(5);
            user.location = cursor.getString(6);
            user.type = cursor.getInt(7);
            user.updated_at = cursor.getString(8);

            users.add(user);
            cursor.moveToNext();
            break;
        }
        cursor.close();
        db.close();

        return users;
    }

    /**
     * Re crate database Delete all tables and create them again
     */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

}
