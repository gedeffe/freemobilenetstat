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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract between the content provider and applications.
 * @author Pixmob
 */
public class NetstatContract {
    /**
     * The authority for the content provider.
     */
    public static final String AUTHORITY = "org.pixmob.freemobile.netstat";
    
    protected static interface PhoneEventsColumns {
        String TIMESTAMP = "timestamp";
        String MOBILE_CONNECTED = "mobile_connected";
        String MOBILE_OPERATOR = "mobile_operator";
        String SYNC_STATUS = "sync_status";
        String SYNC_ID = "sync_id";
    }
    
    protected static interface WifiEventsColumns {
        String TIMESTAMP = "timestamp";
        String WIFI_CONNECTED = "wifi_connected";
        String SYNC_STATUS = "sync_status";
        String SYNC_ID = "sync_id";
    }
    
    protected static interface BatteryEventsColumns {
        String TIMESTAMP = "timestamp";
        String LEVEL = "level";
        String SYNC_STATUS = "sync_status";
        String SYNC_ID = "sync_id";
    }
    
    /**
     * Table for phone events.
     * @author Pixmob
     */
    public static class PhoneEvents implements BaseColumns, PhoneEventsColumns {
        /**
         * The content:// style URI for this table.
         */
        public static final Uri CONTENT_URI = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
                .appendPath("phoneEvents").build();
        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phoneEvent";
        /**
         * The MIME type of {@link #CONTENT_TYPE} providing a directory of
         * entries.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/phoneEvent";
    }
    
    /**
     * Table for Wi-Fi events.
     * @author Pixmob
     */
    public static class WifiEvents implements BaseColumns, WifiEventsColumns {
        /**
         * The content:// style URI for this table.
         */
        public static final Uri CONTENT_URI = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
                .appendPath("wifiEvents").build();
        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/wifiEvent";
        /**
         * The MIME type of {@link #CONTENT_TYPE} providing a directory of
         * entries.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/wifiEvent";
    }
    
    /**
     * Table for battery events.
     * @author Pixmob
     */
    public static class BatteryEvents implements BaseColumns,
            BatteryEventsColumns {
        /**
         * The content:// style URI for this table.
         */
        public static final Uri CONTENT_URI = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
                .appendPath("batteryEvents").build();
        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single
         * entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/batteryEvent";
        /**
         * The MIME type of {@link #CONTENT_TYPE} providing a directory of
         * entries.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/batteryEvent";
    }
}
