package com.siddharth.chatapp;

import android.content.Context;
import android.content.Intent;
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

import java.net.URISyntaxException;

public class login extends ActionBarActivity
{
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

        try
        {
            socket = IO.socket("http://192.168.1.3:80");
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
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

        socket.connect();
    }

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
}