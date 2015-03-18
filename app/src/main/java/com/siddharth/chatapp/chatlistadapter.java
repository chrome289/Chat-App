package com.siddharth.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class chatlistadapter extends BaseAdapter
{
    Activity context;
    ArrayList<String> title;
    ArrayList<String> friend1;
    ArrayList<Bitmap> image;
    ArrayList<String> time;
    ArrayList<Boolean> showDate;

    public chatlistadapter(Activity context, ArrayList<String> title, ArrayList<String> friend1, ArrayList<Bitmap> image, ArrayList<String> time, ArrayList<Boolean> showDate)
    {
        super();
        this.context = context;
        this.title = title;
        this.friend1 = friend1;
        this.image = image;
        this.time = time;
        this.showDate = showDate;
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
        SharedPreferences sharedPref = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");
        int resource;

        //Log.v("","+"+title.get(position).substring(0,8)+"+");
        if (friend1.get(position).equals(username))
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
                TextView aA = (TextView) convertView.findViewById(R.id.textView9);
                if (!showDate.get(position))
                    aA.setVisibility(View.GONE);
                else
                {
                    String t = time.get(position).substring(0, 10);
                    SimpleDateFormat input = new SimpleDateFormat("dd:MM:yyyy");
                    SimpleDateFormat output = new SimpleDateFormat("MMM dd yyyy");
                    try
                    {
                        Date oneWayTripDate = input.parse(t);
                        aA.setText((output.format(oneWayTripDate)));
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                }
                aA = (TextView) convertView.findViewById(R.id.send);
                aA.setText(title.get(position));

                ImageView i = (ImageView) convertView.findViewById(R.id.imageView3);
                if (image.get(position) == null)
                    i.setVisibility(View.INVISIBLE);
                else
                {
                    i.setVisibility(View.VISIBLE);
                    aA.setVisibility(View.GONE);
                }
                i.setImageBitmap(image.get(position));

                aA = (TextView) convertView.findViewById(R.id.textView6);
                String t = time.get(position).substring(11);
                Log.v("dsds", t);
                SimpleDateFormat input = new SimpleDateFormat("HH:mm:SS");
                SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
                try
                {
                    Date oneWayTripDate = input.parse(t);
                    aA.setText((output.format(oneWayTripDate)));
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
                break;
            case R.layout.chatlist2:
                aA = (TextView) convertView.findViewById(R.id.textView8);
                if (!showDate.get(position))
                    aA.setVisibility(View.GONE);
                else
                {
                    t = time.get(position).substring(0, 10);
                    input = new SimpleDateFormat("dd:MM:yyyy");
                    output = new SimpleDateFormat("MMM dd, yyyy");
                    try
                    {
                        Date oneWayTripDate = input.parse(t);
                        aA.setText((output.format(oneWayTripDate)));
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                }
                TextView bB = (TextView) convertView.findViewById(R.id.recieve);
                bB.setText(title.get(position));

                i = (ImageView) convertView.findViewById(R.id.imageView2);
                if (image.get(position) == null)
                    i.setVisibility(View.INVISIBLE);
                else
                {
                    i.setVisibility(View.VISIBLE);
                    bB.setVisibility(View.GONE);
                }
                i.setImageBitmap(image.get(position));

                bB = (TextView) convertView.findViewById(R.id.textView5);
                t = time.get(position).substring(11);
                Log.v("dsds", t);
                input = new SimpleDateFormat("HH:mm:SS");
                output = new SimpleDateFormat("hh:mm aa");
                try
                {
                    Date oneWayTripDate = input.parse(t);
                    bB.setText((output.format(oneWayTripDate)));
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
                break;
        }
        return convertView;
    }
}