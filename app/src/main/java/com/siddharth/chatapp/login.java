package com.siddharth.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.net.URISyntaxException;

public class login extends ActionBarActivity implements View.OnClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress,is_connected;
    private Socket socket;
    public String username = "", password = "";
    public boolean login = false;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

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
        editor.commit();

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
        //mGoogleApiClient.connect();
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
                        Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                        login();
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
                        Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
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


    //login via username login
    public void login()
    {
        EditText e = (EditText) findViewById(R.id.editText2);
        username = String.valueOf(e.getText());
        e = (EditText) findViewById(R.id.editText3);
        password = String.valueOf(e.getText());
        Object[] o = new Object[1];
        o[0] = username;
        socket.emit("storeinfo", o);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    //manual
    public void submit(View view)
    {
        EditText e = (EditText) findViewById(R.id.editText2);
            username = String.valueOf(e.getText());
            e = (EditText) findViewById(R.id.editText3);
            password = String.valueOf(e.getText());
            Object[] args = new Object[2];
            args[0] = username;
            args[1] = password;
            socket.emit("login", args[0], args[1]);
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

        /*if (mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect();
        }*/
    }

    //google login
    @Override
    public void onConnected(Bundle connectionHint)
    {
        mSignInClicked = false;
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Toast.makeText(this, "Welcome " + email, Toast.LENGTH_LONG).show();
        username = email;
        password = "google+";
        Object[] o = new Object[2];
        o[0] = username;
        o[1] = password;
        socket.emit("storeinfo", o);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();
        startActivity(new Intent(this, MainActivity.class));
        finish();
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
    public void onDestroy()
    {
        //google sign out
        Log.v("des","troy");
        if (mGoogleApiClient.isConnected())
        {
            Log.v("des","troy");
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
        super.onDestroy();
    }
}