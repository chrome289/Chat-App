package com.siddharth.chatapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class listviewadapter extends BaseAdapter
{
    Activity context;
    ArrayList<String> title;
    ArrayList<String> subtext;

    public listviewadapter(Activity context, ArrayList<String> title,ArrayList<String>subtext)
    {
        super();
        this.context = context;
        this.title = title;
        this.subtext=subtext;
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
    public void clear()
    {
       title.clear();
        subtext.clear();
    }
    private class ViewHolder
    {
        TextView txtViewTitle;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int resource;

        resource = R.layout.clistview;
        convertView = inflater.inflate(resource, null);
        TextView aA = (TextView) convertView.findViewById(R.id.text);
        aA.setText(title.get(position));
        aA=(TextView)convertView.findViewById(R.id.subtext);
        aA.setText(subtext.get(position));
        return convertView;
    }
}