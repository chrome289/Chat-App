package com.siddharth.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity
{
    public listviewadapter arrayAdapter;
    public Socket socket;
    public String username = "", password = "", send_to = "";
    ArrayList<String> friends = new ArrayList<>();
    ArrayList<String> subtext = new ArrayList<>();
    ArrayList<String> alias = new ArrayList<>();
    ArrayList<Bitmap> profilethumb = new ArrayList<>();
    ArrayList<Long> unread = new ArrayList<>();
    ArrayList<Long> pos = new ArrayList<>();
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Chat App");
        TextView t = (TextView) findViewById(R.id.textView);
        t.setMovementMethod(new ScrollingMovementMethod());
        friends.clear();
        subtext.clear();
        //arrayAdapter.notifyDataSetChanged();
        Log.v("2", "nt");

        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        username = sharedPref.getString("username", "");
        password = sharedPref.getString("password", "");
        editor.putBoolean("handleit", false);
        editor.commit();
        ListView l = (ListView) findViewById(R.id.listView);
        arrayAdapter = new listviewadapter(this, friends, subtext, profilethumb, alias, unread, pos);
        l.setAdapter(arrayAdapter);

        try
        {
            socket = IO.socket("http://192.168.70.1:80");
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

        }).on("messaged2", new Emitter.Listener()
        {

            @Override
            public void call(Object[] args)
            {
                final String temp = String.valueOf(args[0]);
                send_to = (String) args[1];
                final String temp2=String.valueOf(args[2]);
                Log.v("say", "1");
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //Log.v("sgfg", String.valueOf(sharedPref.getBoolean("handleit",false)));
                        if (!sharedPref.getBoolean("handleit", false))
                        {
                            db.execSQL("update user set lastmessage = \"" + send_to + "  :  " + temp + "\" where friend = \"" + send_to + "\"");
                            db.execSQL("insert into '" + send_to + "' values (\"" + send_to + "\" , \"" + username + "\" , \"" + temp + "\" , 1,0,\""+temp2+"\")");
                            Toast.makeText(getApplicationContext(), "recieved", Toast.LENGTH_SHORT).show();
                            int x;
                            //Log.v("fdf", String.valueOf(friends.size()));
                            for (x = 0; x < friends.size(); x++)
                            {
                                if (friends.get(x).equals(send_to))
                                    break;
                            }
                            subtext.set(x, send_to + "  :  " + temp);
                            unread.set(x, unread.get(x) + 1);
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

        }).on("messaged3", new Emitter.Listener()
        {

            @Override
            public void call(Object[] args)
            {
                final String filename = (String) args[0];
                final byte[] decodedString = Base64.decode(((String) args[1]).trim(), Base64.DEFAULT);
                send_to = (String) args[3];
                final String temp2=String.valueOf(args[2]);
                Log.v("say", "1");
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //Log.v("sgfg", String.valueOf(sharedPref.getBoolean("handleit",false)));
                        if (!sharedPref.getBoolean("handleit", false))
                        {
                            //save attachment
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            //save image to sd card
                            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                            final Bitmap b = decodedByte;
                            File myDir = new File(root + "/attachments");
                            myDir.mkdirs();
                            root = filename;
                            final String name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/attachments/" + filename;
                            File file = new File(myDir, root);
                            try
                            {
                                FileOutputStream out = new FileOutputStream(file);
                                b.compress(Bitmap.CompressFormat.PNG, 100, out);
                                out.flush();
                                out.close();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            try
                            {//media scanner
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

                            db.execSQL("update user set lastmessage = \"" + send_to + "  :  Attachment" + "\" where friend = \"" + send_to + "\"");
                            db.execSQL("insert into '" + send_to + "' values (\"" + send_to + "\" , \"" + username + "\" , \"" + name + "\" , 1,1,\""+temp2+"\")");
                            Toast.makeText(getApplicationContext(), "recieved", Toast.LENGTH_SHORT).show();
                            int x;
                            //Log.v("fdf", String.valueOf(friends.size()));
                            for (x = 0; x < friends.size(); x++)
                            {
                                if (friends.get(x).equals(send_to))
                                    break;
                            }

                            subtext.set(x, send_to + "  :  Attachment");
                            unread.set(x, unread.get(x) + 1);
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

        }).on("addfriend", new Emitter.Listener()
        {

            @Override
            public void call(Object[] args)
            {

                //Log.v("lo", String.valueOf(args[0]));
                //convert string thumb to bitmap
                final String temp = String.valueOf(args[0]);
                final String temp1 = String.valueOf(args[2]);
                byte[] decodedString = Base64.decode(((String) args[1]).trim(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                //save image to sd card
                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                final Bitmap b = decodedByte;
                File myDir = new File(root + "/saved_images_thumb");
                myDir.mkdirs();
                root = "Image-" + temp + ".png";
                File file = new File(myDir, root);
                try
                {
                    FileOutputStream out = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {//media scanner
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
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // add details to database
                        Cursor c = db.rawQuery("select * from user where friend = \"" + temp + "\"", null);
                        String mess = "";
                        if (c.getCount() == 0)
                        {
                            db.execSQL("insert into user values(\"" + temp + "\",\"Empty\",\"" + temp + "\",\"" + temp1 + "\",0,1);");
                        }
                        else
                        {
                            c.moveToFirst();
                            db.execSQL("update user set validfriend = 1 where friend=\"" + temp + "\"");
                            mess = c.getString(1);
                        }
                        Log.v("Dasda", "sdfsdf");
                        //update list
                        friends.add(temp);
                        subtext.add(mess);
                        profilethumb.add(b);
                        alias.add(temp1);
                        unread.add((long) 0);
                        db.execSQL("create table if not exists '" + temp + "'('friend1' varchar not null , 'friend2' varchar not null ,'message' varchar,'delivered' integer,'isattach' integer,'time' varchar);");
                        arrayAdapter.notifyDataSetChanged();
                        Object[] u = new Object[2];
                        u[0] = username;
                        u[1] = temp;
                        socket.emit("refresh", u[0], u[1]);
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
        l = (ListView) findViewById(R.id.listView);
        l.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String temp = (String) (((TextView) view.findViewById(R.id.text)).getText());
                editor.putString("send_to", temp);
                temp = (String) (((TextView) view.findViewById(R.id.text1)).getText());
                editor.putString("alias2", temp);
                editor.commit();
                db.execSQL("update user set unread = 0 where friend = \"" + temp + "\"");
                openchat();
            }
        });

    }

    private void intialize()
    {
        Log.v("yu", "ck");
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("create table if not exists user('friend' VARCHAR NOT NULL,'lastmessage' varchar , 'profilethumb' varchar , 'alias' varchar , 'unread' integer default 0,'validfriend' integer);");
        //update list from the local database
        if (!socket.connected())
        {
            Cursor c = db.rawQuery("select * from user where validfriend = 1", null);
            while (c.moveToNext())
            {
                friends.add(c.getString(0));
                subtext.add(c.getString(1));
                alias.add(c.getString(3));
                unread.add((c.getLong(4)));
                String name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/saved_images_thumb/" + "Image-" + c.getString(0) + ".png";
                final Bitmap b = BitmapFactory.decodeFile(name);
                profilethumb.add(b);
                arrayAdapter.notifyDataSetChanged();
            }
        }
        //use server
        else
        {
            Object[] args = new Object[1];
            args[0] = username;
            db.execSQL("update user set validfriend =0 where friend is not \"itjirjtirrihtirbgfbvdbdbg58656554554457\"");
            socket.emit("displayfriends", args[0]);
        }
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
        switch (id)
        {
            case R.id.settings:
                break;
            case R.id.exit:
                android.os.Process.killProcess(Process.myPid());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openchat()
    {
        editor.putBoolean("handleit", true);
        editor.commit();
        startActivity(new Intent(this, chatwin.class));
        //finish();
    }

    @Override
    protected void onResume()
    {

        if (friends.size() > 0)
        {
            friends.clear();
            subtext.clear();
            alias.clear();
            profilethumb.clear();
            unread.clear();
            arrayAdapter.notifyDataSetChanged();
        }
        Log.v("1", "cu");

        intialize();
        super.onResume();
    }
}