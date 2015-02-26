package com.siddharth.chatapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class listviewadapter extends BaseAdapter
{
    Activity context;
    ArrayList<String> title;
    ArrayList<String> subtext;
    ArrayList<Bitmap> profilethumb;
    ArrayList<String> alias;
    ArrayList<Long> unread;
    ArrayList<Long> pos;
    public listviewadapter(Activity context, ArrayList<String> title, ArrayList<String> subtext, ArrayList<Bitmap> profilethumb, ArrayList<String> alias, ArrayList<Long> unread,ArrayList<Long>pos)
    {
        super();
        this.context = context;
        this.title = title;
        this.subtext = subtext;
        this.profilethumb = profilethumb;
        this.alias = alias;
        this.unread = unread;
        this.pos=pos;
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

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int resource;

        resource = R.layout.clistview;
        convertView = inflater.inflate(resource, null);
        TextView aA = (TextView) convertView.findViewById(R.id.text);
        aA.setText(title.get(position));
        aA = (TextView) convertView.findViewById(R.id.subtext);
        aA.setText(subtext.get(position));
        aA = (TextView) convertView.findViewById(R.id.text1);
        aA.setText(alias.get(position));
        aA = (TextView) convertView.findViewById(R.id.textView3);
        aA.setText(String.valueOf(unread.get(position)));
        if (unread.get(position) > 0)
            aA.setVisibility(View.VISIBLE);
        else
            aA.setVisibility(View.INVISIBLE);
        ImageView i = (ImageView) convertView.findViewById(R.id.imageView);
        i.setImageBitmap(profilethumb.get(position));
        return convertView;
    }
}