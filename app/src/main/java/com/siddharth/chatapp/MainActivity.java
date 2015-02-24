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
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity
{
    public ListView l;
    public listviewadapter arrayAdapter;
    public Socket socket;
    public String username = "", password = "", send_to = "";
    ArrayList<String> friends = new ArrayList<>();
    ArrayList<String> subtext = new ArrayList<>();
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
        ListView l = (ListView) findViewById(R.id.listView);
        arrayAdapter = new listviewadapter(this, friends,subtext);
//        l.setDivider(null);
        l.setAdapter(arrayAdapter);

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
                        Cursor c = db.rawQuery("select * from user where friend = \"" + temp + "\"", null);
                        String mess = "";
                        if (c.getCount() == 0)
                        {
                            db.execSQL("insert into user values(\"" + temp + "\",\"Empty\");");
                        }
                        else
                        {
                            c.moveToFirst();
                            mess = c.getString(1);
                        }
                        Log.v("Dasda", "sdfsdf");
                        friends.add(temp);
                        subtext.add(mess);
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
        //intialize();
        //Log.v("4343", "3443");
        l = (ListView) findViewById(R.id.listView);
        l.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String temp = (String)(((TextView) view.findViewById(R.id.text)).getText());
                editor.putString("send_to", temp);
                editor.commit();
                openchat();
            }
        });

    }

    private void intialize()
    {
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("create table if not exists user('friend' VARCHAR NOT NULL,'lastmessage' varchar);");
        db.execSQL("create table if not exists localchat('friend1' varchar not null , 'friend2' varchar not null ,'message' varchar);");
        if (!socket.connected())
        {
            Cursor c = db.rawQuery("select * from user", null);
            while (c.moveToNext())
            {
                friends.add(c.getString(0) + "\n" + c.getString(1));
                updatelist();
            }
        }
        else
        {
            Object[] args = new Object[1];
            args[0] = username;
            socket.emit("displayfriends", args[0]);
        }
    }

    private void updatelist()
    {

        arrayAdapter.notifyDataSetChanged();
//        l.setAdapter(arrayAdapter);
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

    @Override
    protected void onResume()
    {
        if (friends.size() > 0)
        {
            friends.clear();
            subtext.clear();
            arrayAdapter.notifyDataSetChanged();
        }
        intialize();
        super.onResume();
    }
}