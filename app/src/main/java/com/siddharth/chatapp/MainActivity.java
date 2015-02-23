package com.siddharth.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity
{
    public Socket socket;
    public String username = "", password = "", send_to = "";
    ArrayList<String> friends = new ArrayList<>();
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t = (TextView) findViewById(R.id.textView);
        t.setMovementMethod(new ScrollingMovementMethod());

        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        username = sharedPref.getString("username", "");
        password = sharedPref.getString("password", "");

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

        })/*.on("messaged", new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {
                final String temp = String.valueOf(args[0]);
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

        })*/.on("addfriend", new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {
                final String temp = String.valueOf(args[0]);
                Log.v("lo", temp);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        friends.add(temp);
                        Cursor c = db.rawQuery("select * from user where friend = \"" + temp + "\"", null);
                        if (c.getCount() == 0)
                        {
                            db.execSQL("insert into user values(\"" + temp + "\");");
                        }
                        Log.v("Dasda", "sdfsdf");
                        updatelist();
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
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("create table if not exists user('friend' VARCHAR NOT NULL);");
        db.execSQL("create table if not exists localchat('friend1' varchar not null , 'friend2' varchar not null ,'message' varchar);");
        if (!socket.connected())
        {
            Cursor c = db.rawQuery("select * from user", null);
            while (c.moveToNext())
            {
                friends.add(c.getString(0));
                updatelist();
            }
        }
        else
        {
            Object[] args = new Object[1];
            args[0] = username;
            socket.emit("displayfriends", args[0]);
        }
        //Log.v("4343", "3443");
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                editor.putString("send_to", (String) ((TextView) view).getText());
                editor.commit();
                openchat();
            }
        });

    }

    private void updatelist()
    {
        ListView l = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, friends);
        l.setAdapter(arrayAdapter);
    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openchat()
    {
        startActivity(new Intent(this, chatwin.class));
    }

}