package com.siddharth.chatapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class chatlistadapter extends BaseAdapter
{
    Activity context;
    ArrayList<String> title;

    public chatlistadapter(Activity context, ArrayList<String> title)
    {
        super();
        this.context = context;
        this.title = title;
    }

    public int getCount()
    {
        return title.size();
    }

    public Object getItem(int position)
    {
        return null;
    }

    public long getItemId(int position)
    {
        return 0;
    }

    private class ViewHolder
    {
        TextView txtViewTitle;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int resource;

        //Log.v("","+"+title.get(position).substring(0,8)+"+");
        if (title.get(position).substring(0, 8).equals("You  :  "))
        {
            resource = R.layout.chatlist;
        }
        else
        {
            resource = R.layout.chatlist2;
        }
        convertView = inflater.inflate(resource, null);
        switch (resource)
        {
            case R.layout.chatlist:
                TextView aA = (TextView) convertView.findViewById(R.id.send);
                aA.setText(title.get(position));
                break;
            case R.layout.chatlist2:
                TextView bB = (TextView) convertView.findViewById(R.id.recieve);
                bB.setText(title.get(position));
                break;
        }
        return convertView;
    }
}