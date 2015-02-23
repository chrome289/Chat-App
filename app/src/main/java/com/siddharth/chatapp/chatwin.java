package com.siddharth.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;
import java.util.ArrayList;


public class chatwin extends ActionBarActivity
{
    public Socket socket;
    String send_to, username;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPref;
    ArrayList<String> chatlist = new ArrayList<>();
    chatlistadapter arrayAdapter;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatwin);
        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        send_to = sharedPref.getString("send_to", "");
        username = sharedPref.getString("username", "");
        ListView l = (ListView) findViewById(R.id.listView2);
        arrayAdapter = new chatlistadapter(this, chatlist);
        l.setDivider(null);
        l.setAdapter(arrayAdapter);

        try
        {
            socket = IO.socket("http://192.168.1.3:80");
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("create table if not exists " + send_to + "('friend1' varchar not null , 'friend2' varchar not null ,'message' varchar,'delivered' integer);");

        if (socket != null)
        {
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener()
            {
                @Override
                public void call(Object... args)
                {
                    Log.v("con", "nected");
                }

            }).on("messaged", new Emitter.Listener()
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
                            db.execSQL("insert into " + send_to + " values (\"" + send_to + "\" , \"" + username + "\" , \"" + temp + "\" , 1)");
                            chatlist.add(send_to + "  :  " + temp);
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }).on("notsent", new Emitter.Listener()
            {

                @Override
                public void call(final Object... args)
                {
                    Log.v("not", "sent");
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast toast = Toast.makeText(getApplicationContext(),"Message not sent", Toast.LENGTH_SHORT);
                        }
                    });
                }

            }).on("sent", new Emitter.Listener()
            {

                @Override
                public void call(Object... args)
                {
                    final String temp = String.valueOf(args[0]);
                    Log.v("", "sent");
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            db.execSQL("insert into " + send_to + " values (\"" + username + "\" , \"" + send_to + "\" , \"" + temp + "\" , 1)");
                            chatlist.add(("You  :  " + temp));
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }).on("done", new Emitter.Listener()
            {

                @Override
                public void call(Object... args)
                {
                    Log.v("", "done");
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Button b = (Button) findViewById(R.id.button2);
                            b.setEnabled(true);
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
            Cursor c = db.rawQuery("select * from " + send_to, null);
            String temp;
            while (c.moveToNext())
            {
               if (c.getString(0).equals(username))
                {
                    temp = "You  :  " + c.getString(2);

                }
                else
                {
                    temp = send_to + "  :  " +c.getString(2);
                }
                chatlist.add(temp);
            }
            View tview = null;
            refresh(tview);
        }
    }

    //restore previous chat history
    private void restorehistory()
    {
        Object[] args = new Object[2];
        args[0] = username;
        args[1] = send_to;
        socket.emit("restorehistory", args[0], args[1]);
        Button b = (Button) findViewById(R.id.button2);
        b.setEnabled(false);
    }

    //send message
    public void taken(View view)
    {
        String temp;
        Object[] args = new Object[3];
        EditText e = (EditText) findViewById(R.id.editText);
        temp = String.valueOf(e.getText());
        e.setText("");
        args[0] = temp;
        args[1] = username;
        args[2] = send_to;
        socket.emit("takethis", args[0], args[1], args[2]);
    }

    //refresh chat history
    public void refresh(View view)
    {
        Object[] args = new Object[2];
        args[0] = username;
        args[1] = send_to;
        socket.emit("refresh", args[0], args[1]);
        Button b = (Button) findViewById(R.id.button2);
        b.setEnabled(false);
    }
}