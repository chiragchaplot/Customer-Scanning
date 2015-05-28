package com.tseapp.paperbind.customerscanning;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by chiragchaplot on 5/23/15.
 */
public class message_helper extends SQLiteOpenHelper
{
    //DATABASE CREATION SQL STATEMENT

    public message_helper(Context context)
    {
        super (context, message_contract.DB_NAME,null,message_contract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String sqlQuery =
                String.format("CREATE TABLE %s (" +
                                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "%s TEXT,%s TEXT)",
                        message_contract.TABLE,
                        message_contract.Columns.NAME,
                        message_contract.Columns.MESSAGE);
        Log.v("CHIRAGCHAPLOT", sqlQuery);

        db.execSQL(sqlQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }
}
