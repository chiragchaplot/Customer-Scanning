package com.tseapp.paperbind.customerscanning;

import android.provider.BaseColumns;

/**
 * Created by chiragchaplot on 4/13/15.
 */
public class Contact_Contract
{
    public static final String DB_NAME = "com.tseapp.paperbind.contacts.db.entries";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "contacts";

    public class Columns
    {
        public final static String NAME = "name",EMAIL = "email",ADD_CC = "add_cc";
        public static final String _ID = BaseColumns._ID;
    }
}
