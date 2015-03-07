package com.siddharth.chatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;


public class chatwin extends ActionBarActivity
{
    public Socket socket;
    public String send_to, username, alias, picturePath;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPref;
    ArrayList<String> chatlist = new ArrayList<>();
    ArrayList<String> friend1 = new ArrayList<>();
    chatlistadapter arrayAdapter;
    SQLiteDatabase db;
    public int PICK_IMAGE;
    public boolean doit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatwin);
        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        alias = sharedPref.getString("alias2", "");
        send_to = sharedPref.getString("send_to", "");
        username = sharedPref.getString("username", "");
        ListView l = (ListView) findViewById(R.id.listView2);
        arrayAdapter = new chatlistadapter(this, chatlist, friend1);
        l.setDivider(null);
        l.setAdapter(arrayAdapter);

        getSupportActionBar().setTitle(alias);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);

        try
        {
            socket = IO.socket("http://192.168.1.101:80");
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        if (socket != null)
        {
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
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (sharedPref.getBoolean("handleit", false))
                            {
                                Log.v("say", "2");

                                db.execSQL("update user set lastmessage = \"" + send_to + "  :  " + temp + "\" where friend = \"" + send_to + "\"");
                                db.execSQL("insert into '" + send_to + "' values (\"" + send_to + "\" , \"" + username + "\" , \"" + temp + "\" , 1)");
                                chatlist.add(temp);
                                friend1.add(send_to);
                                arrayAdapter.notifyDataSetChanged();
                            }
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
                            Toast.makeText(getApplicationContext(), send_to + " has not added you as friends", Toast.LENGTH_LONG).show();
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
                            db.execSQL("update user set lastmessage = \"You  :  " + temp + "\" where friend = \"" + send_to + "\"");
                            db.execSQL("insert into '" + send_to + "' values (\"" + username + "\" , \"" + send_to + "\" , \"" + temp + "\" , 1)");
                            chatlist.add(temp);
                            friend1.add(username);
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
            Cursor c = db.rawQuery("select * from '" + send_to + "'", null);
            String temp;
            while (c.moveToNext())
            {
                if (c.getString(0).equals(username))
                {
                    temp = c.getString(2);
                    friend1.add(username);

                }
                else
                {
                    temp = c.getString(2);
                    friend1.add(send_to);
                }
                chatlist.add(temp);
                arrayAdapter.notifyDataSetChanged();
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

        Log.v("say", "3");
        Object[] args = new Object[2];
        args[0] = username;
        args[1] = send_to;
        socket.emit("refresh", args[0], args[1]);
        Button b = (Button) findViewById(R.id.button2);
        b.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                editor.putBoolean("handleit", false);
                editor.commit();
                finish();
                return true;
            case R.id.attach:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.v("efrsers", "picturePath");
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null)
        {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
        }
        else
        {
            Toast.makeText(this, "again", Toast.LENGTH_SHORT).show();
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Send Attachment ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        new LoadImage().execute();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onBackPressed()
    {
        Log.v("im", "finished");
        editor.putBoolean("handleit", false);
        editor.commit();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatwin, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //class to upload pic async
    private class LoadImage extends AsyncTask<String, String, Integer>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        protected Integer doInBackground(String... args)
        {
            final String fileName = picturePath, uploadFilePath = "d:/nodejs", uploadFileName = "d***";
            HttpURLConnection conn ;
            DataOutputStream dos ;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(picturePath);
            String filenameArray[] = picturePath.split("\\.");
            String ex = filenameArray[filenameArray.length-1];

            if (!sourceFile.isFile())
            {
                Log.e("uploadFile", "Source File not exist :" + uploadFilePath + "" + uploadFileName);
                Log.v("Source File not exist :", uploadFilePath + "" + uploadFileName);
            }
            else
            {
                try
                {

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);

                    String upLoadServerUri = "http://192.168.1.101/attachments";
                    URL url = new URL(upLoadServerUri);

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("fileUploaded", fileName);
                    conn.setRequestProperty("name", "user");

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + username+" to "+send_to+"."+ex + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0)
                    {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    int serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                    if (serverResponseCode == 200)
                    {

                        Log.v("babola", "dfd");
                    }

                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                }
                catch (MalformedURLException e)
                {
                    e.printStackTrace();
                    Log.e("Upload file to server", "error: " + e.getMessage(), e);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            } // End else bloc
            int a = 0;
            return a;
        }

        protected void onPostExecute(Integer imag)
        {
            doit = true;
        }
    }
}