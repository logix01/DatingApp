package com.enuke.blinder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enuke.blinder.R;
import com.enuke.blinder.Utils.Constants;
import com.enuke.blinder.fragment.MyProfileFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nitesh on 12/29/14.
 */
public class SingleWordDescAdapter extends BaseAdapter {
    ArrayList<HashMap<String, String>> arraylist_desc;
    Context mContext;
    LayoutInflater inflater;

    public SingleWordDescAdapter(Context context,ArrayList<HashMap<String, String>> arraylist){
        mContext = context;
        arraylist_desc = arraylist;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arraylist_desc.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.single_word_desc_grid_item, null);
            holder.mainLayout = (RelativeLayout) view.findViewById(R.id.mainLayout);
            holder.tv_desc = (TextView)view.findViewById(R.id.desc_textview);
            // the setTag is used to store the data within this view
            view.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)view.getTag();
        }

        if(position == MyProfileFragment.mSelectedSingleWordPosition) {
            holder.mainLayout.setBackgroundResource(R.drawable.single_word_desc_selected);
            holder.tv_desc.setTextColor(mContext.getResources().getColor(R.color.white));
        } else {
            
            holder.mainLayout.setBackgroundResource(R.drawable.single_word_desc_grid_item_bg);
            holder.tv_desc.setTextColor(mContext.getResources().getColor(R.color.darkred));
        }
        holder.tv_desc.setText(arraylist_desc.get(position).get(Constants.SINGLE_WORD));

        return view;
    }

    private static class ViewHolder {
        RelativeLayout mainLayout;
        TextView tv_desc;
    }
}
