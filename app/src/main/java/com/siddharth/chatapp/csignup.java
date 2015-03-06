package com.siddharth.chatapp;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class csignup extends Fragment implements View.OnClickListener
{
    private android.os.Handler handler = new android.os.Handler();
    private static final int PICK_IMAGE = 0;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Socket socket;
    public String picturePath = "";
    boolean doit = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.csignup, container, false);
        Button b = (Button) v.findViewById(R.id.button4);
        sharedPref = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        b.setOnClickListener(this);
        b = (Button) v.findViewById(R.id.button5);
        b.setOnClickListener(this);
        try
        {
            socket = IO.socket("http://192.168.1.101:80");
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

        }).on("sign", new Emitter.Listener()
        {
            @Override
            public void call(Object... args)
            {
                final String temp = String.valueOf(args[0]) + "\n";
                Log.v("lo", temp);
                changefrag();
            }
        }).on("wsign", new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ProgressBar p = (ProgressBar) getView().findViewById(R.id.pb);
                        p.setVisibility(View.INVISIBLE);
                        Button b = (Button) getView().findViewById(R.id.button4);
                        b.setEnabled(true);
                        b = (Button) getView().findViewById(R.id.button5);
                        b.setEnabled(true);
                        Toast.makeText(getActivity().getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT);
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
        return v;
    }

    private void changefrag()
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Runnable r = new Runnable()
                {
                    public void run()
                    {
                        if (!doit)
                            handler.postDelayed(this, 1000);
                        else
                        {
                            ProgressBar p = (ProgressBar) getView().findViewById(R.id.pb);
                            p.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity().getApplicationContext(), "Registeration complete", Toast.LENGTH_SHORT);
                            clogin newFragment = new clogin();
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.content_frame, newFragment, "clogin");
                            ft.commit();
                            getFragmentManager().executePendingTransactions();
                        }
                    }
                };
                handler.postDelayed(r, 1000);
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button4:
                Log.v("erer", "ytyty");
                Object[] o = new Object[4];
                o[0] = ((EditText) getView().findViewById(R.id.editText4)).getText();
                o[1] = ((EditText) getView().findViewById(R.id.editText5)).getText();
                o[2] = ((EditText) getView().findViewById(R.id.editText6)).getText();
                o[3] = ((EditText) getView().findViewById(R.id.editText7)).getText();
                socket.emit("signup", o);
                ProgressBar p = (ProgressBar) getView().findViewById(R.id.pb);
                p.setVisibility(View.VISIBLE);
                Button b = (Button) getView().findViewById(R.id.button4);
                b.setEnabled(false);
                b = (Button) getView().findViewById(R.id.button5);
                b.setEnabled(false);
                new LoadImage().execute();
                break;
            case R.id.button5:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE);
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null)
        {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            Log.v("efrsers", picturePath);
            cursor.close();
        }
        else
        {
            Toast.makeText(getActivity(), "again", Toast.LENGTH_SHORT).show();
        }

    }

    //class to download pic async
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
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(picturePath);

            if (!sourceFile.isFile())
            {
                Log.e("uploadFile", "Source File not exist :"+ uploadFilePath + "" + uploadFileName);
                Log.v("Source File not exist :",uploadFilePath + "" + uploadFileName);
            }
            else
            {
                try
                {

                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);

                    String upLoadServerUri = "http://192.168.1.101";
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
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + ((EditText) getView().findViewById(R.id.editText4)).getText() + "\"" + lineEnd);

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

                    Log.i("uploadFile", "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

                    if (serverResponseCode == 200)
                    {

                        Log.v("babola", "dfd");
                    }

                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                }
                catch (MalformedURLException ex)
                {
                    ex.printStackTrace();
                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
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