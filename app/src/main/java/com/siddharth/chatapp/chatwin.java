package com.siddharth.chatapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class chatwin extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public Socket socket;
    public String send_to, username, alias, picturePath, location = "";
    SharedPreferences.Editor editor;
    SharedPreferences sharedPref;
    ArrayList<String> chatlist = new ArrayList<>();
    ArrayList<String> friend1 = new ArrayList<>();
    ArrayList<Bitmap> image = new ArrayList<>();
    ArrayList<String> time = new ArrayList<>();
    ArrayList<String> uploaded_imagepath = new ArrayList<>();
    ArrayList<Boolean> showDate = new ArrayList<>();
    chatlistadapter arrayAdapter;
    SQLiteDatabase db;
    public int PICK_IMAGE;
    public boolean doit = false;
    public GoogleApiClient mGoogleApiClient;
    static final String AB = "0123456789qwertyuioplkjhgfdsazxcvbnm";
    static Random rnd = new Random();

    String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatwin);
        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        if (!sharedPref.contains("date"))
            editor.putString("date", "");
        editor.putString("date", "");
        alias = sharedPref.getString("alias2", "");
        send_to = sharedPref.getString("send_to", "");
        username = sharedPref.getString("username", "");
        ListView l = (ListView) findViewById(R.id.listView2);
        arrayAdapter = new chatlistadapter(this, chatlist, friend1, image, time, showDate);
        l.setDivider(null);
        l.setAdapter(arrayAdapter);

        getSupportActionBar().setTitle(alias);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);

        //getting location
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        try {
            socket = IO.socket(login.ServerAddress);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        if (socket != null) {
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.v("con", "nected");
                }

            }).on("takeTimeLoc", new Emitter.Listener() {

                @Override
                public void call(Object[] args) {
                    final String temp = String.valueOf(args[0]);
                    Log.v("time", "12");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //getSupportActionBar().setSubtitle(Html.fromHtml("<small>Last seen at " +temp+"</small>"));
                        }
                    });
                }

            }).on("messaged2", new Emitter.Listener() {

                @Override
                public void call(Object[] args) {
                    final String temp = String.valueOf(args[0]);
                    final String temp2 = String.valueOf(args[2]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (sharedPref.getBoolean("handleit", false)) {
                                if ((temp2.substring(0, 10)).compareTo(sharedPref.getString("date", "")) != 0) {
                                    showDate.add(true);
                                    editor.putString("date", temp2.substring(0, 10));
                                    editor.commit();
                                } else
                                    showDate.add(false);
                                Log.v("say", "2");
                                db.execSQL("update user set lastmessage = \"" + send_to + "  :  " + temp + "\" where friend = \"" + send_to + "\"");
                                db.execSQL("insert into '" + send_to + "' values (\"" + send_to + "\" , \"" + username + "\" , \"" + temp + "\" , 1,0,\"" + temp2 + "\")");
                                chatlist.add(temp);
                                friend1.add(send_to);
                                image.add(null);
                                time.add(temp2);
                                arrayAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }).on("messaged3", new Emitter.Listener() {
                @Override
                public void call(Object[] args) {
                    final String filename = String.valueOf(args[0]);
                    final byte[] decodedString = Base64.decode(((String) args[1]).trim(), Base64.DEFAULT);
                    final String temp2 = String.valueOf(args[2]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (sharedPref.getBoolean("handleit", false)) {
                                Log.v("say", "2");

                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                //save image to sd card
                                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                                final Bitmap b = decodedByte;
                                File myDir = new File(root + "/attachments");
                                myDir.mkdirs();
                                root = filename;
                                final String name = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/attachments/" + filename;
                                File file = new File(myDir, root);
                                try {
                                    FileOutputStream out = new FileOutputStream(file);
                                    b.compress(Bitmap.CompressFormat.PNG, 100, out);
                                    out.flush();
                                    out.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try {//media scanner
                                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.toString()}, null,
                                            new MediaScannerConnection.OnScanCompletedListener() {
                                                public void onScanCompleted(String path, Uri uri) {
                                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                                    Log.i("ExternalStorage", "-> uri=" + uri);
                                                }
                                            });
                                } catch (Exception e) {
                                }
                                //update list
                                db.execSQL("update user set lastmessage = \"" + send_to + "  :  " + filename + "\" where friend = \"" + send_to + "\"");
                                db.execSQL("insert into '" + send_to + "' values (\"" + send_to + "\" , \"" + username + "\" , \"" + filename + "\" , 1,1,\"" + temp2 + "\")");
                                if ((temp2.substring(0, 10)).compareTo(sharedPref.getString("date", "")) != 0) {
                                    showDate.add(true);
                                    editor.putString("date", temp2.substring(0, 10));
                                    editor.commit();
                                } else
                                    showDate.add(false);
                                chatlist.add(name);
                                friend1.add(send_to);
                                image.add(b);
                                time.add(temp2);
                                arrayAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }).on("notsent", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {
                    Log.v("not", "sent");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), send_to + " has not added you as friends", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }).on("sent", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    final String temp = String.valueOf(args[0]);
                    final int n = (int) args[1];
                    final String temp2 = String.valueOf(args[2]);
                    Log.v("", "sent");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ((temp2.substring(0, 10)).compareTo(sharedPref.getString("date", "")) != 0) {
                                showDate.add(true);
                                editor.putString("date", temp2.substring(0, 10));
                                editor.commit();
                            } else
                                showDate.add(false);
                            if (n == 1) {
                                Bitmap myBitmap = BitmapFactory.decodeFile(uploaded_imagepath.get(0));
                                image.add(myBitmap);
                                db.execSQL("update user set lastmessage = \"You  :  " + uploaded_imagepath.get(0) + "\" where friend = \"" + send_to + "\"");
                                db.execSQL("insert into '" + send_to + "' values (\"" + username + "\" , \"" + send_to + "\" , \"" + uploaded_imagepath.get(0) + "\" , 1," + n + ",\"" + temp2 + "\")");
                                chatlist.add(uploaded_imagepath.get(0));
                                friend1.add(username);
                                time.add(temp2);
                                uploaded_imagepath.remove(0);
                            } else {
                                image.add(null);
                                db.execSQL("update user set lastmessage = \"You  :  " + temp + "\" where friend = \"" + send_to + "\"");
                                db.execSQL("insert into '" + send_to + "' values (\"" + username + "\" , \"" + send_to + "\" , \"" + temp + "\" , 1," + n + ",\"" + temp2 + "\")");
                                chatlist.add(temp);
                                friend1.add(username);
                                time.add(temp2);
                            }
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });
                }

            }).on("done", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.v("", "done");
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.v("dis", String.valueOf(args[0]));
                }

            });
            socket.connect();
            l = (ListView) findViewById(R.id.listView2);



            l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Bitmap i=image.get(position);
                    if(image.get(position)!=null) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Log.v("gil", chatlist.get(position));
                        intent.setDataAndType(Uri.fromFile(new File(chatlist.get(position))), "image/*");
                        startActivity(intent);
                    }
                    else{
                        Log.v("gile", String.valueOf(image.get(position)));
                    }
                }
            });
            restorehistory();
            refresh();

            Object[]o=new Object[1];
            o[0]=send_to;
            socket.emit("tellTimeLoc", o);
        }
    }

    //restore previous chat history
    private void restorehistory() {
        Cursor c = db.rawQuery("select * from \"" + send_to + "\"", null);
        while (c.moveToNext()) {
            friend1.add(c.getString(0));
            time.add(c.getString(5));
            if (c.getInt(4) == 1) {
                Bitmap bitmap = BitmapFactory.decodeFile(c.getString(2));
                image.add(bitmap);
                chatlist.add(c.getString(2));
            } else {
                image.add(null);
                chatlist.add(c.getString(2));
            }
            if ((c.getString(5).substring(0, 10)).compareTo(sharedPref.getString("date", "")) != 0) {
                showDate.add(true);
                editor.putString("date", c.getString(5).substring(0, 10));
                editor.commit();
            } else
                showDate.add(false);
        }

        arrayAdapter.notifyDataSetChanged();
    }

    //send message
    public void taken(View view) {
        String temp;
        Object[] args = new Object[4];
        EditText e = (EditText) findViewById(R.id.editText);
        temp = String.valueOf(e.getText());
        e.setText("");
        args[0] = temp;
        args[1] = username;
        args[2] = send_to;
        args[3] = 0;
        socket.emit("takethis", args);

    }

    //refresh chat history
    public void refresh() {

        Log.v("say", "3");
        Object[] args = new Object[2];
        args[0] = username;
        args[1] = send_to;
        socket.emit("refresh", args[0], args[1]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                editor.putBoolean("handleit", false);
                editor.commit();
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                break;
            case R.id.attach:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE);
                break;
            case R.id.location:
                EditText e = (EditText) findViewById(R.id.editText);
                e.setText(e.getText()+" "+location);
                break;
            case R.id.refresh:
                refresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("efrsers", "picturePath");
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
        } else {
            Toast.makeText(this, "again", Toast.LENGTH_SHORT).show();
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Send Attachment ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new LoadImage().execute();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        Log.v("im", "finished");
        editor.putBoolean("handleit", false);
        editor.commit();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatwin, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient= new GoogleApiClient.Builder(this.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.v("loc", String.valueOf(mLastLocation.getLatitude()));
            Log.v("loc", String.valueOf(mLastLocation.getLongitude()));
            Geocoder gcd = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(addresses!=null) {
                if (addresses.size() > 0) {
                    Log.v("loc", addresses.get(0).getLocality() + addresses.get(0).getCountryName());
                    location = addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    //class to upload pic async
    private class LoadImage extends AsyncTask<String, String, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Integer doInBackground(String... args) {
            final String fileName = picturePath, uploadFilePath = "d:/nodejs", uploadFileName = "d***";
            HttpURLConnection conn;
            DataOutputStream dos;
            String lineEnd = "\r\n", temp = "", twoHyphens = "--", boundary = "*****";
            byte[] buffer;
            int maxBufferSize = 1024 * 1024, bytesRead, bytesAvailable, bufferSize;
            File sourceFile = new File(picturePath);
            String filenameArray[] = picturePath.split("\\.");
            String ex = filenameArray[filenameArray.length - 1];

            if (!sourceFile.isFile()) {
                Log.e("uploadFile", "Source File not exist :" + uploadFilePath + "" + uploadFileName);
                Log.v("Source File not exist :", uploadFilePath + "" + uploadFileName);
            } else {
                try {

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);

                    String upLoadServerUri = login.ServerAddress+"/attachments";
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

                    temp = randomString(30);
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + temp + "." + username + "." + send_to + "." + ex + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
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

                    if (serverResponseCode == 200) {
                        Log.v("babola", "dfd");
                    }

                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.e("Upload file to server", "error: " + e.getMessage(), e);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } // End else bloc
            int a = 0;

            //adding message to server
            Object[] arg = new Object[4];
            arg[0] = temp + "." + ex;
            arg[1] = username;
            arg[2] = send_to;
            arg[3] = 1;
            uploaded_imagepath.add(picturePath);
            socket.emit("takethis", arg[0], arg[1], arg[2], arg[3]);
            return a;
        }

        protected void onPostExecute(Integer imag) {
            doit = true;
        }
    }
}