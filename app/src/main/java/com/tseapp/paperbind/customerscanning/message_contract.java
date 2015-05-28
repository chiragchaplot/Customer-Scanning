package com.tseapp.paperbind.customerscanning;

import android.provider.BaseColumns;

/**
 * Created by chiragchaplot on 5/23/15.
 */
public class message_contract
{
    public static final String DB_NAME = "com.tseapp.paperbind.message.db.entries";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "message";

    public class Columns
    {
        public final static String NAME = "name",MESSAGE = "message";
        public static final String _ID = BaseColumns._ID;
    }
}
