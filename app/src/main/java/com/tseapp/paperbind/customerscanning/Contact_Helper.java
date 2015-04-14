package com.tseapp.paperbind.customerscanning;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by chiragchaplot on 4/13/15.
 */

/*
    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database,int oldVersion,int newVersion){
        Log.w(MyDatabaseHelper.class.getName(),
                         "Upgrading database from version " + oldVersion + " to "
                         + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS MyEmployees");
        onCreate(database);
    }
}

 */
public class Contact_Helper extends SQLiteOpenHelper
{
    //DATABASE CREATION SQL STATEMENT

    public Contact_Helper(Context context)
    {
        super (context, Contact_Contract.DB_NAME,null,Contact_Contract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String sqlQuery =
                String.format("CREATE TABLE %s (" +
                                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT,%s TEXT, %s TEXT)",
                        Contact_Contract.TABLE,
                        Contact_Contract.Columns.NAME,
                        Contact_Contract.Columns.EMAIL,
                        Contact_Contract.Columns.ADD_CC);
        Log.v("CHIRAGCHAPLOT",sqlQuery);

        db.execSQL(sqlQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }
}
