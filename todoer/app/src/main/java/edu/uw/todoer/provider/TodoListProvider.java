package edu.uw.todoer.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * A Content Provider giving access to a database of tasks.
 * Each task has a title, details, deadline, and completed status
 *
 * @author Joel Ross
 */
public class TodoListProvider extends ContentProvider {

    private static final String TAG = "TodoProvider";

    //Content Provider details
    private static final String AUTHORITY = "edu.uw.todoer.provider";
    private static final String TASK_RESOURCE = "tasks";

    //URI details
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"+TASK_RESOURCE);

    //database details
    private static final String DATABASE_NAME = "todolist.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * The schema and contract for the underlying database.
     */
    public static class TaskEntry implements BaseColumns {
        //class cannot be instantiated
        private TaskEntry(){}

        public static final String TABLE_NAME = "tasks";
        public static final String COL_TITLE = "title";
        public static final String COL_DETAILS = "details";
        public static final String COL_TIME_CREATED = "created_at";
        public static final String COL_DEADLINE = "deadline";
        public static final String COL_COMPLETED = "completed";
    }

    private static final UriMatcher sUriMatcher; //for handling Uri requests

    //integer values representing each supported resource Uri
    private static final int TASKS_URI = 1; // /tasks
    private static final int TASKS_NUM_URI = 2;// /tasks/:id

    static {
        //setup mapping between URIs and IDs
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, TASK_RESOURCE, TASKS_URI);
        sUriMatcher.addURI(AUTHORITY, TASK_RESOURCE + "/#", TASKS_NUM_URI);
    }


    /**
     * A class to help open, create, and update the database
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String CREATE_TASKS_TABLE =
                "CREATE TABLE " + TaskEntry.TABLE_NAME + "(" +
                        TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", "+
                        TaskEntry.COL_TIME_CREATED + " INTEGER" + ","+
                        TaskEntry.COL_TITLE + " TEXT" + "," +
                        TaskEntry.COL_DETAILS + " TEXT" + ","+
                        TaskEntry.COL_DEADLINE + " INTEGER" + ","+
                        TaskEntry.COL_COMPLETED + " INTEGER" +
                ")";

        private static final String DROP_TASKS_TABLE = "DROP TABLE IF EXISTS "+TaskEntry.TABLE_NAME;

        public DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(TAG, "Creating tasks table");
            db.execSQL(CREATE_TASKS_TABLE); //create table if needed
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TASKS_TABLE); //just drop and recreate table
            onCreate(db);
        }
    }

    private DatabaseHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext()); //initialize the helper
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //build a query for us
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TaskEntry.TABLE_NAME);

        //projection mapping would go here if needed

        switch(sUriMatcher.match(uri)){
            case TASKS_URI: //all tasks
                //no change
                break;
            case TASKS_NUM_URI: //single task
                builder.appendWhere(TaskEntry._ID + "=" + uri.getPathSegments().get(1)); //restrict to those items
                //numeric data so not need to escape
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }

        //open the database
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();

        //now pass in the user arguments
        Cursor c = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //validate uri
        if(sUriMatcher.match(uri) != TASKS_URI) {
            throw new IllegalArgumentException("Unknown URI "+uri);
        }

        //make sure all fields are set
        if(!values.containsKey(TaskEntry.COL_TITLE)){
            values.put(TaskEntry.COL_TITLE, "Untitled");
        }

        if(!values.containsKey(TaskEntry.COL_DETAILS)){
            values.put(TaskEntry.COL_DETAILS, "");
        }

        if(!values.containsKey(TaskEntry.COL_DEADLINE)){
            values.put(TaskEntry.COL_DEADLINE, System.currentTimeMillis());
        }

        if(!values.containsKey(TaskEntry.COL_COMPLETED)){
            values.put(TaskEntry.COL_COMPLETED, 0);
        }

        //created now, no matter what
        values.put(TaskEntry.COL_TIME_CREATED, System.currentTimeMillis());

        //open the database
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        long rowId = db.insert(TaskEntry.TABLE_NAME, null, values);

        if (rowId > 0) { //if successful
            Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri; //return the URI for the entry
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //open the database;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        int count;
        switch (sUriMatcher.match(uri)) {
            case TASKS_URI:
                count = db.delete(TaskEntry.TABLE_NAME, selection, selectionArgs); //just pass in params
                break;
            case TASKS_NUM_URI:
                String taskId = uri.getPathSegments().get(1);
                count = db.delete(TaskEntry.TABLE_NAME, TaskEntry._ID + "=" + taskId //select by id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs); //apply params
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //open the database;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();

        int count;
        switch (sUriMatcher.match(uri)) {
            case TASKS_URI:
                count = db.update(TaskEntry.TABLE_NAME, values, selection, selectionArgs); //just pass in params
                break;
            case TASKS_NUM_URI:
                String taskId = uri.getPathSegments().get(1);
                count = db.update(TaskEntry.TABLE_NAME, values, TaskEntry._ID + "=" + taskId //select by id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs); //apply params
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        //return cursor types, per http://developer.android.com/guide/topics/providers/content-provider-creating.html#TableMIMETypes
        switch(sUriMatcher.match(uri)){
            case TASKS_URI:
                return "vnd.android.cursor.dir/"+AUTHORITY+"."+TASK_RESOURCE;
            case TASKS_NUM_URI:
                return "vnd.android.cursor.item/"+AUTHORITY+"."+TASK_RESOURCE;
            default:
                throw new IllegalArgumentException("Unknown URI "+uri);
        }
    }
}
