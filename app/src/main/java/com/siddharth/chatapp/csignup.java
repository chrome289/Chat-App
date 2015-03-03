package com.siddharth.chatapp;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;


public class csignup extends Fragment implements View.OnClickListener
{
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Socket socket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.csignup, container, false);
        Button b = (Button) v.findViewById(R.id.button4);
        sharedPref = getActivity().getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
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
                Toast.makeText(getActivity().getApplicationContext(), "Registeration complete", Toast.LENGTH_SHORT);
                clogin newFragment = new clogin();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, newFragment, "clogin");
                ft.commit();
                getFragmentManager().executePendingTransactions();
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
                o[0] = "9450659962";//=((EditText) getView().findViewById(R.id.editText4)).getText();
                o[1] = "Sanjay Seth";//=((EditText) getView().findViewById(R.id.editText5)).getText();
                o[2] = "sanjayset64h@gmail.com";//=((EditText) getView().findViewById(R.id.editText6)).getText();
                o[3] = "2";//=((EditText) getView().findViewById(R.id.editText7)).getText();
                socket.emit("signup", o);
        }
    }
}