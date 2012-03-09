/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
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
package org.pixmob.freemobile.netstat.provider;

import static org.pixmob.freemobile.netstat.Constants.TAG;

import java.util.ArrayList;

import org.pixmob.freemobile.netstat.provider.NetstatContract.Events;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * The content provider for the application database.
 * @author Pixmob
 */
public class NetstatContentProvider extends ContentProvider {
    private static final String EVENTS_TABLE = "events";
    
    private static final int EVENTS = 1;
    private static final int EVENT_ID = 2;
    
    private static final UriMatcher URI_MATCHER;
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(NetstatContract.AUTHORITY, "events", EVENTS);
        URI_MATCHER.addURI(NetstatContract.AUTHORITY, "event/*", EVENT_ID);
    }
    
    private SQLiteOpenHelper dbHelper;
    
    @Override
    public boolean onCreate() {
        try {
            dbHelper = new DatabaseHelper(getContext());
        } catch (Exception e) {
            Log.e(TAG, "Cannot create content provider", e);
            return false;
        }
        return true;
    }
    
    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case EVENTS:
                return Events.CONTENT_TYPE;
            case EVENT_ID:
                return Events.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
    }
    
    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        if (operations.isEmpty()) {
            return new ContentProviderResult[0];
        }
        
        // Execute batch operations in a single transaction for performance.
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }
    
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final long rowId = db.insertOrThrow(EVENTS_TABLE, "notNull", values);
        if (rowId == -1) {
            throw new SQLException("Failed to insert new event");
        }
        
        final Uri eventUri = Uri.withAppendedPath(Events.CONTENT_URI,
            String.valueOf(rowId));
        getContext().getContentResolver().notifyChange(uri, null, false);
        
        return eventUri;
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int count;
        
        switch (URI_MATCHER.match(uri)) {
            case EVENTS:
                count = db.delete(EVENTS_TABLE, selection, selectionArgs);
                break;
            case EVENT_ID:
                final String id = uri.getPathSegments().get(1);
                String fullSelection = Events._ID + "='" + id + "'";
                if (!TextUtils.isEmpty(selection)) {
                    fullSelection += " AND (" + selection + ")";
                }
                count = db.delete(EVENTS_TABLE, fullSelection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        String realSortOrder = sortOrder;
        
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case EVENTS:
                qb.setTables(EVENTS_TABLE);
                if (TextUtils.isEmpty(realSortOrder)) {
                    realSortOrder = Events.TIMESTAMP + " DESC";
                }
                break;
            case EVENT_ID:
                qb.setTables(EVENTS_TABLE);
                qb.appendWhere(Events._ID + "=" + uri.getPathSegments().get(1));
                break;
        }
        
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final Cursor c = qb.query(db, projection, selection, selectionArgs,
            null, null, realSortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        
        return c;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int count;
        switch (URI_MATCHER.match(uri)) {
            case EVENTS:
                count = db.update(EVENTS_TABLE, values, selection,
                    selectionArgs);
                break;
            case EVENT_ID:
                final String id = uri.getPathSegments().get(1);
                String fullSelection = Events._ID + "='" + id + "'";
                if (!TextUtils.isEmpty(selection)) {
                    fullSelection += " AND (" + selection + ")";
                }
                count = db.update(EVENTS_TABLE, values, fullSelection,
                    selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri: " + uri);
        }
        
        getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }
    
    /**
     * This class is responsible for managing the application database. The
     * database schema is initialized when the it is created, and upgraded after
     * an application update.
     * @author Pixmob
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(final Context context) {
            super(context, "netstat.db", null, 1);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            if (!db.isReadOnly()) {
                final String req = "CREATE TABLE " + EVENTS_TABLE + " ("
                        + Events._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + Events.TIMESTAMP + " TIMESTAMP NOT NULL, "
                        + Events.MOBILE_ENABLED + " INTEGER NOT NULL, "
                        + Events.MOBILE_ROAMING + " INTEGER NOT NULL, "
                        + Events.SYNC_ID + " TEXT NOT NULL, "
                        + Events.SYNC_STATUS + " INTEGER NOT NULL, "
                        + Events.MOBILE_OPERATOR + " TEXT)";
                db.execSQL(req);
            }
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (!db.isReadOnly()) {
                Log.w(TAG, "Upgrading database from version " + oldVersion
                        + " to " + newVersion + " which will destroy all data");
                db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
                onCreate(db);
            }
        }
    }
}
