package com.tseapp.paperbind.customerscanning;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends ActionBarActivity {

    public String email, result, phone, name, db_name, db_email, db_cc;
    public AlertDialog send_email;
    public Boolean res;
    public Button scan, view, form_submit, offline;
    public EditText cust_name, cust_email;
    public CheckBox send_to_vasu;
    public Boolean scan_done, vasu_sent;
    public List<String> email_list = new ArrayList<String>();
    public Cursor cursor;
    public Contact_Helper helper = new Contact_Helper(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SQLiteDatabase sqLB = new Contact_Helper(this).getWritableDatabase();
        cursor = sqLB.query(Contact_Contract.TABLE,
                new String[]{Contact_Contract.Columns._ID, Contact_Contract.Columns.NAME, Contact_Contract.Columns.EMAIL, Contact_Contract.Columns.ADD_CC},
                null, null, null, null, null);

        cursor.moveToFirst();

        buildUI();
    }

    public void buildUI() {
        scan = (Button) findViewById(R.id.scan);
        view = (Button) findViewById(R.id.view);
        offline = (Button) findViewById(R.id.offline);
        form_submit = (Button) findViewById(R.id.chirag);


        scan.setOnClickListener
                (

                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (haveNetworkConnection()) {
                                    IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                                    integrator.initiateScan();
                                    scan_done = true;
                                } else {
                                    no_network_alert();
                                }
                            }
                        }
                );


        view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (haveNetworkConnection()) {
                            getemails e = new getemails();
                            e.execute();
                        } else {
                            Toast.makeText(getApplicationContext(), "NO INTERNET ACCESS", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        form_submit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (haveNetworkConnection()) {
                            scan_done = false;
                            formview();
                        } else {
                            no_network_alert();
                        }
                    }
                }
        );

        offline.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        show_database();
                    }
                }
        );


    }

    //SHow offline database
    public void show_database() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.list, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Offline Database");
        ListView lv = (ListView) convertView.findViewById(R.id.lv);

        helper = new Contact_Helper(MainActivity.this);
        SQLiteDatabase sqlDB = helper.getReadableDatabase();
        Cursor cursor = sqlDB.query(Contact_Contract.TABLE,
                new String[]{Contact_Contract.Columns._ID, Contact_Contract.Columns.EMAIL, Contact_Contract.Columns.NAME, Contact_Contract.Columns.ADD_CC},
                null, null, null, null, null);

        final ListAdapter listAdapter = new SimpleCursorAdapter
                (
                        MainActivity.this,
                        R.layout.list_item,
                        cursor,
                        new String[]{Contact_Contract.Columns.EMAIL, Contact_Contract.Columns.ADD_CC, Contact_Contract.Columns.NAME},
                        new int[]{R.id.email, R.id.cc, R.id.name},
                        0);

        lv.setAdapter(listAdapter);

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                email_list.clear();
            }
        });

        alertDialog.show();
        lv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                    }
                }
        );
        Toast.makeText(getApplicationContext(), String.valueOf(listAdapter.getCount()) + " Entries", Toast.LENGTH_SHORT).show();
    }


    //Check for net connection
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    //Network Dialog notification
    public void no_network_alert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("No Internet Connectivity");
        builder.setMessage("No Network Connection Available. However, would you like to enter to the database?\n\n(All the contacts will be uploaded to server as soon as a stable internet connection is observed)");
        builder.setIcon(R.drawable.ic_launcher);
        builder.setPositiveButton("Enter to Database", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //All of the fun happens inside the CustomListener now.
                //I had to move it to enable data validation.
                enter_to_database();
            }
        });


        send_email = builder.create();
        send_email.show();
    }

    //Form for no internet access to database
    public void enter_to_database() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.form, null);
        cust_email = (EditText) convertView.findViewById(R.id.email);
        cust_name = (EditText) convertView.findViewById(R.id.name);
        send_to_vasu = (CheckBox) convertView.findViewById(R.id.vasu);


        alertDialog.setView(convertView);
        alertDialog.setTitle("Enter Customer Details");
        alertDialog.setIcon(R.drawable.ic_launcher);


        alertDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (send_to_vasu.isChecked()) {
                    vasu_sent = true;

                } else {
                    vasu_sent = false;
                }
                insert_record();
            }
        });

        send_email = alertDialog.create();
        send_email.show();
    }

    //Insert Record to DB
    public void insert_record() {
        helper = new Contact_Helper(MainActivity.this);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.clear();
        values.put(Contact_Contract.Columns.ADD_CC, vasu_sent.toString());
        values.put(Contact_Contract.Columns.NAME, cust_name.getText().toString());
        values.put(Contact_Contract.Columns.EMAIL, cust_email.getText().toString());

        long l = db.insertWithOnConflict(Contact_Contract.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        Log.v("CHIRAGCHAPLOT", String.valueOf(l));
    }

    //Alert Dialog for the form button
    public void formview() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.form, null);
        cust_email = (EditText) convertView.findViewById(R.id.email);
        cust_name = (EditText) convertView.findViewById(R.id.name);
        send_to_vasu = (CheckBox) convertView.findViewById(R.id.vasu);


        alertDialog.setView(convertView);
        alertDialog.setTitle("Enter Customer Details");
        alertDialog.setIcon(R.drawable.ic_launcher);


        alertDialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (send_to_vasu.isChecked()) {
                    vasu_sent = true;

                } else {
                    vasu_sent = false;
                }


                upload_contact update = new upload_contact();
                update.execute();


            }
        });

        send_email = alertDialog.create();
        send_email.show();
    }

    public class upload_contact extends AsyncTask<Void, Void, String> {

        ProgressDialog p = new ProgressDialog(MainActivity.this);
        public boolean check;

        @Override
        protected void onPreExecute() {
            send_email.dismiss();
            p.setTitle("Sending Email");
            p.setMessage("Sending Email to " + cust_email.getText().toString());
            p.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String addresslist = null;
            try {
                final String BASE_URL =
                        "http://rishvatkhori.com/app/upload_contact.php?";
                final String PARAM_EMAIL = "email";
                final String PARAM_PHONE = "name";
                final String PARAM_ADD_CC = "add_cc";
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(PARAM_EMAIL, cust_email.getText().toString())
                        .appendQueryParameter(PARAM_PHONE, cust_name.getText().toString())
                        .appendQueryParameter(PARAM_ADD_CC, vasu_sent.toString())
                        .build();

                Log.v("CHIRAGCHAPLOT", builtUri.toString());
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                addresslist = buffer.toString();

            } catch (IOException e) {
                Log.e("ERRORLOG", "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("ERRORLOG", "Error closing stream ", e);
                    }
                }
            }

            try {
                Log.v("CHIRAGCHAPLOT", addresslist.toString());
                return getresult(addresslist);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        public String getresult(String result) throws JSONException {
            JSONObject message = new JSONObject(result);
            String done = message.getString("message");
            if (done.equals("1")) {
                check = true;
            } else {
                check = false;
            }

            return done;
        }

        @Override
        protected void onPostExecute(String done) {
            if (p.isShowing()) {
                email = cust_email.getText().toString();
                name = cust_name.getText().toString();
                p.dismiss();
                if (check == true) {
                    email_sent();
                } else {
                    email_not_sent();
                }
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            result = scanResult.getContents();
            Log.d("code", result);
            Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(result);
            while (m.find()) {
                Log.v("email", m.group());
                email = m.group();
            }
            m = Pattern.compile("\\+\\d{4,}").matcher(result);
            while (m.find()) {
                Log.v("phone", m.group());
                phone = m.group();
            }

            show_alert();
        }
        // else continue with any other code you need in the method
    }

    public void show_alert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Details Extracted");
        builder.setMessage("Email: " + email + "\n" + "Phone: " + phone);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setPositiveButton("Send Email", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //All of the fun happens inside the CustomListener now.
                //I had to move it to enable data validation.
                send_email sent = new send_email();
                sent.execute();

            }
        });


        send_email = builder.create();
        send_email.show();
    }

    public void email_sent() {
        AlertDialog yes;

        final AlertDialog.Builder positive = new AlertDialog.Builder(MainActivity.this);
        positive.setTitle("EMAIL SENT");
        positive.setMessage("Email sent to " + email);
        positive.setIcon(R.drawable.ic_launcher);
        positive.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        yes = positive.create();
        yes.show();
    }

    public void email_not_sent() {
        final AlertDialog.Builder negative = new AlertDialog.Builder(MainActivity.this);
        negative.setTitle("EMAIL NOT SENT");
        negative.setMessage("Email not sent to " + email);
        negative.setIcon(R.drawable.ic_launcher);
        negative.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                send_email hey = new send_email();
                hey.execute();
            }
        });

        AlertDialog no;
        no = negative.create();
        no.show();

    }


    public class send_email extends AsyncTask<Void, Void, String> {

        ProgressDialog p = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            send_email.dismiss();
            p.setTitle("Sending Email");
            p.setMessage("Sending Email to " + email);
            p.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String addresslist = null;
            try {
                final String BASE_URL =
                        "http://rishvatkhori.com/app/send_email.php?";
                final String PARAM_EMAIL = "email";
                final String PARAM_PHONE = "phone";
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(PARAM_EMAIL, email)
                        .appendQueryParameter(PARAM_PHONE, phone)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                addresslist = buffer.toString();

            } catch (IOException e) {
                Log.e("ERRORLOG", "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("ERRORLOG", "Error closing stream ", e);
                    }
                }
            }

            try {
                Log.v("CHIRAGCHAPLOT", addresslist.toString());
                return getresult(addresslist);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        public String getresult(String result) throws JSONException {
            JSONObject message = new JSONObject(result);
            String done = message.getString("message");
            if (done.equals("1")) {
                res = true;
            } else {
                res = false;
            }

            return done;
        }

        @Override
        protected void onPostExecute(String done) {
            if (p.isShowing()) {
                p.dismiss();
                if (res == true) {
                    email_sent();
                } else {
                    email_not_sent();
                }
            }
        }

    }

    public class getemails extends AsyncTask<Void, Void, String> {

        ProgressDialog p = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            p.setTitle("Fetching Emails");
            p.setMessage("Fetchings Email from server");
            p.setIcon(R.drawable.ic_launcher);
            p.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String addresslist = null;
            try {
                final String BASE_URL =
                        "http://rishvatkhori.com/app/scan_get.php?";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                addresslist = buffer.toString();

            } catch (IOException e) {
                Log.e("ERRORLOG", "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("ERRORLOG", "Error closing stream ", e);
                    }
                }
            }

            try {
                Log.v("CHIRAGCHAPLOT", addresslist.toString());
                return getresult(addresslist);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public String getresult(String result) throws JSONException {
            JSONObject message = new JSONObject(result);
            String done = message.getString("message");
            if (done.equals("1")) {
                JSONArray emails = message.getJSONArray("emails");
                for (int i = 0; i < emails.length(); i++) {
                    JSONObject email_address = emails.getJSONObject(i);
                    email_list.add(email_address.getString("email"));
                }
                res = true;
            } else {
                res = false;
            }

            return done;
        }

        @Override
        protected void onPostExecute(String done) {
            if (p.isShowing()) {
                p.dismiss();
                if (res == true) {
                    email_received();
                } else {
                    email_not_sent();
                }
            }
        }
    }

    public void email_received() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.list, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("List");
        ListView lv = (ListView) convertView.findViewById(R.id.lv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, email_list);
        lv.setAdapter(adapter);

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                email_list.clear();
            }
        });

        alertDialog.show();
    }

    public void check_to_send()
    {
        helper = new Contact_Helper(MainActivity.this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from contacts", null);

        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false && haveNetworkConnection()) {

                //Get the values
                db_name = cursor.getString(cursor.getColumnIndex(Contact_Contract.Columns.NAME));
                db_email = cursor.getString(cursor.getColumnIndex(Contact_Contract.Columns.EMAIL));
                db_cc = cursor.getString(cursor.getColumnIndex(Contact_Contract.Columns.ADD_CC));

                db.delete(Contact_Contract.TABLE, Contact_Contract.Columns.EMAIL + " =?", new String[]{db_email});
            }

            cursor.moveToNext();
        }
    }

    public class send_database_contacts extends AsyncTask<Void,Void, String>
    {
        public boolean res;

        @Override
        protected String doInBackground(Void... params)
        {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String addresslist = null;
            try
            {
                final String BASE_URL =
                        "http://rishvatkhori.com/app/upload_contact.php?";
                final String PARAM_EMAIL = "email";
                final String PARAM_PHONE = "name";
                final String PARAM_ADD_CC = "add_cc";
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(PARAM_EMAIL, db_email)
                        .appendQueryParameter(PARAM_PHONE,db_name)
                        .appendQueryParameter(PARAM_ADD_CC,db_cc)
                        .build();

                Log.v("CHIRAGCHAPLOT",builtUri.toString());
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null)
                {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line = reader.readLine())!=null)
                {
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0)
                {
                    return null;
                }

                addresslist = buffer.toString();

            }
            catch (IOException e)
            {
                Log.e("ERRORLOG","Error ",e);
                return null;
            }

            finally
            {
                if (urlConnection!=null)
                {
                    urlConnection.disconnect();
                }

                if(reader!=null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch(IOException e)
                    {
                        Log.e("ERRORLOG","Error closing stream ",e);
                    }
                }
            }

            try
            {
                Log.v("CHIRAGCHAPLOT",addresslist.toString());
                return getresult(addresslist);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        public String getresult(String result) throws JSONException
        {
            JSONObject message = new JSONObject(result);
            String done = message.getString("message");
            if(done.equals("1"))
            {
                res = true;
            }
            else
            {
                res = false;
            }

            return done;
        }

    }

}