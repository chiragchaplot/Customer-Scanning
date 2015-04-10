package com.tseapp.paperbind.customerscanning;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends ActionBarActivity
{

    public String email,result,phone;
    public AlertDialog send_email;
    public Boolean res;
    Button scan, view;
    List <String> email_list = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildUI();
    }

    public void buildUI()
    {
        scan = (Button) findViewById(R.id.scan);
        view = (Button) findViewById(R.id.view);

        scan.setOnClickListener
        (

                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                                integrator.initiateScan();
                            }
                        }
        );


        view.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        getemails e = new getemails();
                        e.execute();
                    }
                }
        );
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null)
        {
            result = scanResult.getContents();
            Log.d("code", result);
            Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(result);
            while (m.find()) {
                Log.v("email",m.group());
                email = m.group();
            }
            m = Pattern.compile("\\+\\d{4,}").matcher(result);
            while(m.find())
            {
                Log.v("phone",m.group());
                phone = m.group();
            }

            show_alert();
        }
        // else continue with any other code you need in the method
    }

    public void show_alert()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Details Extracted");
        builder.setMessage("Email: " + email + "\n" + "Phone: " + phone) ;
        builder.setIcon(R.drawable.ic_launcher);
        builder.setPositiveButton("Send Email", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                //All of the fun happens inside the CustomListener now.
                //I had to move it to enable data validation.
                send_email sent = new send_email();
                sent.execute();

            }
        });


        send_email = builder.create();
        send_email.show();
    }

    public void email_sent()
    {
        AlertDialog yes;

        final AlertDialog.Builder positive = new AlertDialog.Builder(MainActivity.this);
        positive.setTitle("EMAIL SENT");
        positive.setMessage("Email sent to " + email);
        positive.setIcon(R.drawable.ic_launcher);
        positive.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });

        yes = positive.create();
        yes.show();
    }

    public void email_not_sent()
    {
        final AlertDialog.Builder negative = new AlertDialog.Builder(MainActivity.this);
        negative.setTitle("EMAIL NOT SENT");
        negative.setMessage("Email not sent to " + email);
        negative.setIcon(R.drawable.ic_launcher);
        negative.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                send_email hey = new send_email();
                hey.execute();
            }
        });

        AlertDialog no;
        no = negative.create();
        no.show();

    }


    public class send_email extends AsyncTask<Void,Void, String>
    {

        ProgressDialog p = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute()
        {
            send_email.dismiss();
            p.setTitle("Sending Email");
            p.setMessage("Sending Email to " + email);
            p.show();
        }
        @Override
        protected String doInBackground(Void... params)
        {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String addresslist = null;
            try
            {
                final String BASE_URL =
                        "http://rishvatkhori.com/app/send_email.php?";
                final String PARAM_EMAIL = "email";
                final String PARAM_PHONE = "phone";
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(PARAM_EMAIL, email)
                        .appendQueryParameter(PARAM_PHONE,phone)
                        .build();

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

        @Override
        protected void onPostExecute(String done)
        {
            if(p.isShowing())
            {
                p.dismiss();
                if(res == true)
                {
                    email_sent();
                }
                else
                {
                    email_not_sent();
                }
            }
        }

    }

    public class getemails extends AsyncTask<Void,Void,String >
    {

        ProgressDialog p = new ProgressDialog(MainActivity.this);
        @Override
        protected void onPreExecute()
        {
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
            try
            {
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
                JSONArray emails = message.getJSONArray("emails");
                for (int i=0;i<emails.length();i++)
                {
                    JSONObject email_address = emails.getJSONObject(i);
                    email_list.add(email_address.getString("email"));
                }
                res = true;
            }
            else
            {
                res = false;
            }

            return done;
        }

        @Override
        protected void onPostExecute(String done)
        {
            if(p.isShowing())
            {
                p.dismiss();
                if(res == true)
                {
                    email_received();
                }
                else
                {
                    email_not_sent();
                }
            }
        }
    }

    public void email_received()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.list, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("List");
        ListView lv = (ListView) convertView.findViewById(R.id.lv);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,email_list);
        lv.setAdapter(adapter);

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {

            }
        });

        alertDialog.show();
    }
}
