package com.siddharth.chatapp;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;


public class clogin extends Fragment implements View.OnClickListener
{
    private android.os.Handler handler = new android.os.Handler();
    private Socket socket;
    String username, password, fri = "", alias = "", email = "";
    public boolean login = false;
    int a = 0, c = 0;
    Bitmap image;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.clogin, container, false);
        Button b = (Button) v.findViewById(R.id.button3);
        sharedPref = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        b.setOnClickListener(this);
        b = (Button) v.findViewById(R.id.button4);
        b.setOnClickListener(this);
        CheckBox c = (CheckBox) v.findViewById(R.id.checkBox);
        c.setOnClickListener(this);
        if (c.isChecked())
            editor.putBoolean("remember", true);
        else
            editor.putBoolean("remember", false);

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

        }).on("message", new Emitter.Listener()
        {
            @Override
            public void call(Object... args)
            {
                final String temp = String.valueOf(args[0]) + "\n";
                Log.v("lo", temp);
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(getActivity().getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).on("listupdated", new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        login = true;
                        Log.v("fri", "ends");
                        Toast.makeText(getActivity().getApplicationContext(), "All set", Toast.LENGTH_SHORT).show();
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
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        login = true;
                        get_contacts();
                        Object[] t = new Object[2];
                        t[0] = username;
                        t[1] = fri;
                        Log.v("sds", "fri");
                        socket.emit("checkuserexist", t);
                        CheckBox c = (CheckBox) getView().findViewById(R.id.checkBox);
                        if (c.isChecked())
                            editor.putBoolean("remember", true);
                        else
                            editor.putBoolean("remember", false);
                        editor.commit();
                        Toast.makeText(getActivity().getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).on("ready", new Emitter.Listener()
        {
            @Override
            public void call(Object... args)
            {
                Object[] t = new Object[2];
                t[0] = username;
                t[1] = fri;
                Log.v("end", "end");
                //Toast.makeText(getActivity().getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
            }

        }).on("wlogin", new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {
                final String temp = String.valueOf(args[0]) + "\n";
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Log.v("lo", temp);
                        login = false;
                        editor.putString("username", "");
                        editor.putString("password", "");
                        editor.putString("alias", "");
                        editor.putBoolean("remember", false);
                        editor.commit();
                        Button b = (Button) getView().findViewById(R.id.button3);
                        b.setEnabled(true);
                        b = (Button) getView().findViewById(R.id.button4);
                        b.setEnabled(true);
                        ProgressBar p = (ProgressBar) getView().findViewById(R.id.pb);
                        p.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity().getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
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
        if (sharedPref.getBoolean("remember", false))
        {
            Object[] o = new Object[2];
            o[0] = sharedPref.getString("username", "");
            o[1] = sharedPref.getString("password", "");
            username=sharedPref.getString("username", "");
            password = sharedPref.getString("password", "");
            socket.emit("login", o[0], o[1]);
        }
        return v;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button3:
                EditText e = (EditText) getView().findViewById(R.id.editText2);
                username = String.valueOf(e.getText());
                e = (EditText) getView().findViewById(R.id.editText3);
                password = String.valueOf(e.getText());
                Button b = (Button) getView().findViewById(R.id.button3);
                b.setEnabled(false);
                b = (Button) getView().findViewById(R.id.button4);
                b.setEnabled(false);
                ProgressBar p = (ProgressBar) getView().findViewById(R.id.pb);
                p.setVisibility(View.VISIBLE);
                //username = "8435013374";
                //password = "1";
                Object[] o = new Object[2];

                if (username.length() == 0 || password.length() == 0)
                {
                    o[0] = sharedPref.getString("username", "");
                    o[1] = sharedPref.getString("password", "");
                    Toast.makeText(getActivity().getApplicationContext(), "Wrong Credentials", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (sharedPref.getBoolean("remember", false))
                    {
                        Log.v("howdy", "ihyihih");
                        o[0] = username;
                        o[1] = password;
                        editor.putString("username", username);
                        editor.putString("password", password);
                        editor.commit();
                        socket.emit("login", o[0], o[1]);
                    }
                    else
                    {
                        o[0] = username;
                        o[1] = password;
                        editor.putString("username", username);
                        editor.putString("password", password);
                        editor.commit();
                        socket.emit("login", o[0], o[1]);
                    }
                }
                break;
            case R.id.button4:
                csignup newFragment = new csignup();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, newFragment, "csignup");
                ft.commit();
                getFragmentManager().executePendingTransactions();
                break;
            case R.id.checkBox:
                CheckBox c = (CheckBox) getView().findViewById(R.id.checkBox);
                if (c.isChecked())
                    editor.putBoolean("remember", true);
                else
                    editor.putBoolean("remember", false);
                editor.commit();
                break;
        }
    }

    //get contacts
    private void get_contacts()
    {
        Log.v("cal", "led");
        Cursor cursor = null;
        try
        {
            cursor = getActivity().getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            int phoneNumberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            cursor.moveToFirst();
            do
            {
                String temp = cursor.getString(phoneNumberIdx), temp2;
                temp2 = temp.replaceAll("\\D+", "");
                if (temp2.length() != 10)
                    temp2 = temp2.substring(temp2.length() - 10);
                fri = temp2 + "," + fri;
            }
            while (cursor.moveToNext());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.v("here", "1");
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        c = 1;
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
                Log.v("here", "2");
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
                    Log.v("here", "3");
                }

            }
            else
            {
                Log.v("error", "fdfd");
            }
            try
            {
                //media scanner
                MediaScannerConnection.scanFile(getActivity().getApplicationContext(), new String[]{file.toString()}, null,
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
                Log.v("here", "4");
            }
            a++;
        }
    }

    private void slogin()
    {
        Object[] o = new Object[4];
        o[0] = username;
        o[1] = password;
        o[2] = alias;
        o[3] = email;
        socket.emit("storeinfo", o);
        a = 2;
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Runnable r = new Runnable()
                {
                    public void run()
                    {
                        if (a == 2 && c == 1)
                        {
//                          Toast.makeText(getActivity().getApplicationContext(), "Welcome back", Toast.LENGTH_SHORT).show();
                            Log.v("hiui", String.valueOf(a));
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.putString("alias", alias);
                            editor.commit();
                            //findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                            getActivity().finish();
                        }
                        else
                            handler.postDelayed(this, 1000);
                    }
                };
                handler.postDelayed(r, 1000);
            }
        });
    }
}