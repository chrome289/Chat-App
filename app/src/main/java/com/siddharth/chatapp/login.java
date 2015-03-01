package com.siddharth.chatapp;


import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class login extends ActionBarActivity implements View.OnClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult>
{
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    Handler h = new Handler();
    String fri = "";
    private boolean mIntentInProgress;
    int a = 0, b = 0;
    private Socket socket;
    public String username = "", password = "", alias = "", email = "";
    public boolean login = false;
    SharedPreferences sharedPref;
    File file;
    SharedPreferences.Editor editor;
    Bitmap image = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        if (!sharedPref.contains("username"))
            editor.putString("username", "");
        if (!sharedPref.contains("password"))
            editor.putString("password", "");
        if (!sharedPref.contains("send_to"))
            editor.putString("send_to", "");
        if (!sharedPref.contains("alias"))
            editor.putString("alias", "");
        if (!sharedPref.contains("alias2"))
            editor.putString("alias2", "");
        if (!sharedPref.contains("firstuse"))
            editor.putBoolean("firstuse", true);
        if (!sharedPref.contains("handleit"))
            editor.putBoolean("handleit", false);

        editor.commit();

        if (sharedPref.getBoolean("firstuse", true))
        {
            TextView t = (TextView) findViewById(R.id.textView2);
            t.setText("Welcome\n\nSign up using your Google Account");
            RelativeLayout r = (RelativeLayout) findViewById(R.id.loadingPanel);
            r.setVisibility(View.INVISIBLE);
        }
        //connecting socket
        try
        {
            socket = IO.socket("http://192.168.1.3:80");
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        socket.connect();

        //assigning google sign in button listen
        SignInButton btnSignIn = (SignInButton) findViewById(R.id.sign_in);
        btnSignIn.setOnClickListener(this);

        //google sign in client initialization
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        mGoogleApiClient.connect();

        //socket events
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener()
        {
            @Override
            public void call(Object... args)
            {
                Log.v("con", "nected");
            }

        }).on("message", new Emitter.Listener()
        {
            @Override
            public void call(Object... args)
            {
                final String temp = String.valueOf(args[0]) + "\n";
                Log.v("lo", temp);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).on("listupdated", new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        login = true;
                        Log.v("fri", "ends");
                        Toast.makeText(getApplicationContext(), "All set", Toast.LENGTH_SHORT).show();
                        slogin();
                    }
                });
            }

        }).on("login", new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {
                final String temp = String.valueOf(args[0]) + "\n";
                Log.v("lo", temp);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        login = true;
                        //slogin();
                        Object[] t = new Object[2];
                        t[0] = username;
                        t[1] = fri;
                        Log.v("sds", fri);
                        socket.emit("checkuserexist", t);
                        Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).on("wlogin", new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {
                final String temp = String.valueOf(args[0]) + "\n";
                Log.v("lo", temp);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        login = false;
                        editor.putString("username", "");
                        editor.putString("password", "");
                        editor.putString("alias", "");
                        editor.commit();
                        SignInButton btnSignIn = (SignInButton) findViewById(R.id.sign_in);
                        btnSignIn.setEnabled(false);
                        Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable()
                        {
                            public void run()
                            {
                                finish();
                            }
                        }, 2000);
                    }
                });
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {
                Log.v("dis", String.valueOf(args[0]));
            }

        });
    }

    private void slogin()
    {
        h.postDelayed(new Runnable()
        {
            public void run()
            {
                if (a == 2)
                {
                    Log.v("hiui", String.valueOf(a));
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.putString("alias", alias);
                    editor.commit();
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
                else h.postDelayed(this, 1000);
            }
        }, 3000);
    }


    //google client start
    protected void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
    }

    //client stop
    protected void onStop()
    {
        super.onStop();

        //uncomment for persistent login

        if (mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect();
        }
    }

    //google login
    @Override
    public void onConnected(Bundle connectionHint)
    {
        editor.putBoolean("firstuse", false);
        editor.commit();
        TextView t = (TextView) findViewById(R.id.textView2);
        t.setText("Loading Contacts");
        RelativeLayout r = (RelativeLayout) findViewById(R.id.loadingPanel);
        r.setVisibility(View.VISIBLE);

        mSignInClicked = false;
        //parse photo url
        email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Person p = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        username = p.getId();
        password = "google+";
        alias = p.getDisplayName();
        Person.Image s = p.getImage();
        String temp = s.getUrl().substring(0, s.getUrl().length() - 2);
        String temp2 = temp + "50";
        temp = temp.concat("300");

        Log.v(username, temp);
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

        Plus.PeopleApi.loadVisible(mGoogleApiClient, null).setResultCallback(this);

        //save picture
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        String fname = "Image-" + email + ".png";
        file = new File(myDir, fname);
        if (!file.exists()) new LoadImage().execute(temp);
        else
            a++;


        //save thumb
        File myDir2 = new File(root + "/saved_images_thumb");
        myDir.mkdirs();
        String fname2 = "Image-" + email + ".png";
        File file2 = new File(myDir2, fname2);
        if (!file2.exists())
            new LoadImage().execute(temp2);
        else
            a++;


        Object[] o = new Object[6];
        o[0] = username;
        o[1] = password;
        o[2] = temp;
        o[3] = temp2;
        o[4] = alias;
        o[5] = email;
        socket.emit("storeinfo", o);
        Toast.makeText(getApplicationContext(), "Welcome back", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int cause)
    {
        mGoogleApiClient.connect();
    }

    public void onClick(View view)
    {
        if (view.getId() == R.id.sign_in && !mGoogleApiClient.isConnecting())
        {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /* Track whether the sign-in button has been clicked so that we know to resolve
 * all issues preventing sign-in without waiting.
 */
    private boolean mSignInClicked;

    /* Store the connection result from onConnectionFailed callbacks so that we can
     * resolve them when the user clicks sign-in.
     */
    private ConnectionResult mConnectionResult;

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError()
    {
        if (mConnectionResult.hasResolution())
        {
            try
            {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            }
            catch (IntentSender.SendIntentException e)
            {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    public void onConnectionFailed(ConnectionResult result)
    {
        if (!mIntentInProgress)
        {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            mConnectionResult = result;

            if (mSignInClicked)
            {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent)
    {
        if (requestCode == RC_SIGN_IN)
        {
            if (responseCode != RESULT_OK)
            {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting())
            {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData)
    {
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS)
        {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            try
            {
                int count = personBuffer.getCount();
                for (int i = 0; i < count; i++)
                {
                    fri = fri + personBuffer.get(i).getId().toString() + ",";
                }
            }
            finally
            {
                personBuffer.close();
            }
        }
        else
        {
            Log.e("TAG", "Error requesting visible circles: " + peopleData.getStatus());
        }
    }


    //class to download pic async
    private class LoadImage extends AsyncTask<String, String, Bitmap>
    {
        File file;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... args)
        {
            Bitmap bitmap = null;
            try
            {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap imag)
        {
            if (imag != null)
            {
                image = imag;
                try
                {
                    FileOutputStream out = new FileOutputStream(file);
                    imag.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            else
            {
                Log.v("error", "fdfd");
            }
            try
            {
                //media scanner
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener()
                        {
                            public void onScanCompleted(String path, Uri uri)
                            {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });
            }
            catch (Exception e)
            {
            }
            a++;
        }
    }
}