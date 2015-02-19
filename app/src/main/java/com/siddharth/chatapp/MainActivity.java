package com.siddharth.chatapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public class MainActivity extends ActionBarActivity
{
    public Socket socket;public String username="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView t=(TextView)findViewById(R.id.textView);
        t.setMovementMethod(new ScrollingMovementMethod());
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Enter Username");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                username= String.valueOf(input.getText());
            }
        });
        alert.setCancelable(false);
        alert.show();
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
                Log.v("con","nected");
            }

        }).on("messaged", new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {
                Log.v("232", String.valueOf(args[0]));
            }

        }).on(Socket.EVENT_MESSAGE, new Emitter.Listener()
        {

            @Override
            public void call(Object... args)
            {
                final String temp= String.valueOf(args[0])+"\n";
                Log.v("Re", temp);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        TextView t = (TextView) findViewById(R.id.textView);
                        t.setText(t.getText()+temp);
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

    public void foo(View view) throws IOException, URISyntaxException
    {
        //new RequestTask().execute("http://192.168.1.3:80");
        socket.connect();
        Object[] o=new Object[1];
        o[0]=username;
        socket.emit("storeinfo",o);
    }

    class RequestTask extends AsyncTask<String, String, String>
    {

        @Override
        protected String doInBackground(String... uri)
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try
            {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    Log.v("dfs", responseString);
                    out.close();
                }
                else
                {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            }
            catch (ClientProtocolException e)
            {
                Log.v("dfs", "1");
            }
            catch (IOException e)
            {
                Log.v("dfs", "2");
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
            //Do anything with response..
            TextView t = (TextView) findViewById(R.id.textView);
            t.setText(result);
        }
    }

    public void bar(View view)
    {
        socket.disconnect();
        TextView t = (TextView) findViewById(R.id.textView);
        t.setText(t.getText()+"Disconnected\n");

    }

    public void taken(View view)
    {
        Object[] args=new Object[2];
        EditText e=(EditText)findViewById(R.id.editText);
        args[0]=e.getText();
        args[1]=username;
        socket.emit("takethis", args[0],args[1]);
    }

}