package com.siddharth.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;




public class chatwin extends ActionBarActivity
{
    public Socket socket;
    String send_to,username;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatwin);
        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        send_to = sharedPref.getString("send_to", "");
        username = sharedPref.getString("username", "");
        TextView t = (TextView) findViewById(R.id.textView);
        t.setMovementMethod(new ScrollingMovementMethod());
        try
        {
            socket = IO.socket("http://192.168.1.3:80");
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
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
                    final String temp = String.valueOf(args[0]) + "\n\n";
                    Log.v("lo", temp);
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TextView t = (TextView) findViewById(R.id.textView);
                            t.setText(t.getText() + temp);
                            Log.v("asdas", "sdfsdf");
                        }
                    });
                }

            }).on("notsent", new Emitter.Listener()
            {

                @Override
                public void call(final Object... args)
                {
                    final String temp = String.valueOf(args[0]) + "\n\n";
                    Log.v("not", "sent");
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TextView t = (TextView) findViewById(R.id.textView);
                            t.setText(t.getText() + send_to+" has not yet recieved your last message " + "\n\n");
                        }
                    });
                }

            }).on("sent", new Emitter.Listener()
            {

                @Override
                public void call(Object... args)
                {
                    final String temp = String.valueOf(args[0]) + "\n\n";
                    Log.v("", "sent");
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            TextView t = (TextView) findViewById(R.id.textView);
                            t.setText(t.getText()+String.valueOf("You  :  "+temp));
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
                            Button b=(Button)findViewById(R.id.button2);
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
        }
    }

    public void taken(View view)
    {
        String temp;
        Object[] args = new Object[3];
        EditText e = (EditText) findViewById(R.id.editText);
        temp= String.valueOf(e.getText());
        e.setText("");
        args[0] = temp;
        args[1]=username;
        args[2] = send_to;
        socket.emit("takethis", args[0],args[1] ,args[2]);
    }
    public void refresh(View view)
    {
        Object[]args=new Object[2];
        args[0]=username;
        args[1]=send_to;
        socket.emit("refresh",args[0],args[1]);
        Button b=(Button)findViewById(R.id.button2);
        b.setEnabled(false);
    }
}