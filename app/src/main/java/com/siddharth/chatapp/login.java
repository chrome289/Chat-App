package com.siddharth.chatapp;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class login extends ActionBarActivity
{
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
public static String ServerAddress= "http://9a3e2fb9.ngrok.io";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        sharedPref = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        if (!sharedPref.contains("username"))
            editor.putString("username", "");
        if (!sharedPref.contains("password"))
            editor.putString("password", "");
        if (!sharedPref.contains("send_to"))
            editor.putString("send_to", "");
        if (!sharedPref.contains("alias"))
            editor.putString("alias", "");
        if (!sharedPref.contains("alias2"))
            editor.putString("alias2", "");
        if (!sharedPref.contains("firstuse"))
            editor.putBoolean("firstuse", true);
        if (!sharedPref.contains("handleit"))
            editor.putBoolean("handleit", false);
        if (!sharedPref.contains("remember"))
            editor.putBoolean("remember", false);
        editor.commit();

        if (sharedPref.getBoolean("firstuse", true))
        {
            //TextView t = (TextView) findViewById(R.id.textView2);
            //t.setText("Welcome\n\nSign up using your Google Account");
            //RelativeLayout r = (RelativeLayout) findViewById(R.id.loadingPanel);
            //r.setVisibility(View.INVISIBLE);
        }


        //initialize the view
        clogin newFragment = new clogin();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, newFragment, "clogin");
        ft.commit();
        getFragmentManager().executePendingTransactions();

    }


}