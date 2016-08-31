package com.enuke.blinder.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enuke.blinder.R;

import java.util.ArrayList;

/**
 * Created by enuke on 12/9/14.
 */
public class NavigationDrawerAdapter extends BaseAdapter {

    int[] _images_unselected = {R.drawable.nav_myprofile_icon, R.drawable.nav_mymatch_icon, R.drawable.nav_chat_icon, R.drawable.nav_rateus_icon};

    String[] _name={"My Profile", "My Matches", "Chats", "Rate Us"};


    LayoutInflater inflater;
    Context mContext;

    ArrayList<String> selected_item = new ArrayList<String>();

    public NavigationDrawerAdapter(Context context, ArrayList<String> selectedItem){
        mContext=context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.selected_item = selectedItem;
    }


    @Override
    public int getCount() {
        return _name.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;

        //check to see if the reused view is null or not, if is not null then reuse it
        if (view == null) {
            holder = new ViewHolder();

            view = inflater.inflate(R.layout.navigation_drawer_item, null);
            holder.itemName = (TextView) view.findViewById(R.id.nav_title);
            holder.image = (ImageView) view.findViewById(R.id.nav_image);
            holder.navLayout = (RelativeLayout) view.findViewById(R.id.nav_layout);
            // the setTag is used to store the data within this view
            view.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)view.getTag();
        }

        if(selected_item.get(i).equalsIgnoreCase("true")) {
            holder.navLayout.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
        } else {
            holder.navLayout.setBackgroundColor(mContext.getResources().getColor(R.color.nav_bg));
        }

        holder.itemName.setText(_name[i]);
        holder.itemName.setTextColor(Color.WHITE);
        holder.image.setImageResource(_images_unselected[i]);

        return view;
    }

    private static class ViewHolder {
        RelativeLayout navLayout;
        protected TextView itemName;
        ImageView image;

    }
}
