package com.enuke.blinder.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enuke.blinder.Entity.ChatHomeEntity;
import com.enuke.blinder.R;
import com.enuke.blinder.Utils.Utility;
import com.enuke.blinder.database.AvatarTable;
import com.enuke.blinder.database.DBHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.xabber.android.ui.register.Constansts;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by enuke on 12/29/14.
 */
public class ChatsAdapter extends BaseAdapter {

    ArrayList<ChatHomeEntity> arraylist_mymatches;
    Context mContext;
    LayoutInflater inflater;
    DBHelper db;
    String today = "";

    public ChatsAdapter(Context context, ArrayList<ChatHomeEntity> arraylist){
        mContext = context;
        arraylist_mymatches = arraylist;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        db = new DBHelper(mContext);
        today = Utility.getDate(Calendar.getInstance().getTimeInMillis());
    }

    @Override
    public int getCount() {
        return arraylist_mymatches.size();
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
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final ViewHolder holder;

        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.messages_list_item, null);
            holder.iv_avatar = (ImageView) view.findViewById(R.id.message_avatar_image);
            holder.screenName = (TextView) view.findViewById(R.id.message_title);
            holder.messagePreview = (TextView) view.findViewById(R.id.message_subtitle);
            holder.chatTime = (TextView) view.findViewById(R.id.chat_time);
            holder.chatExtra = (TextView) view.findViewById(R.id.chat_new);
            holder.mainLayout = (RelativeLayout) view.findViewById(R.id.message_main_layout);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }

        if(position%2 == 0) {
            holder.mainLayout.setBackgroundResource(R.color.red);
        } else {
            holder.mainLayout.setBackgroundResource(R.color.darkred);
        }

        try {
            ChatHomeEntity chat = arraylist_mymatches.get(position);
            final String userId = chat.getUserid();
            String screenName = chat.getScreenname();
            String message = chat.getLatestmessage();
            if(!TextUtils.isEmpty(message)) {
                if(message.contains(Constansts.IMAGE_TAG)) {
                    message = "Image";
                } else if(message.contains(Constansts.AUDIO_TAG)) {
                    message = "Audio";
                } else if(message.contains(Constansts.VIDEO_TAG)) {
                    message = "Video";
                }
            }
            String time = chat.getTime();
            if(time.equalsIgnoreCase("null") || TextUtils.isEmpty(time)) {
                time = "";
            } else {
                String date = Utility.getDate(Long.valueOf(time));
                if(date.equalsIgnoreCase(today)) {
                    holder.chatTime.setText(Utility.formatTimeString(mContext, Long.valueOf(time)));
                } else {
                    holder.chatTime.setText(date);
                }
            }

            String extraMessage = chat.getExtraMessage();
            if(extraMessage.equalsIgnoreCase("new")) {
                holder.chatExtra.setText("NEW");
                holder.chatExtra.setVisibility(View.VISIBLE);
            } else {
                holder.chatExtra.setVisibility(View.GONE);
            }

            String read = chat.getRead();
            if(read.equalsIgnoreCase("1")) {
                holder.messagePreview.setTextColor(mContext.getResources().getColor(R.color.white));
            } else {
                holder.messagePreview.setTextColor(mContext.getResources().getColor(R.color.nav_bg));
            }

            holder.screenName.setText(screenName);
            holder.messagePreview.setText(message);
            String avatarPath = Utility.getApplicationStoragePath() + AvatarTable.getInstance().getAvatarName(arraylist_mymatches.get(position).getAvatarcode());
            ImageLoader.getInstance().displayImage("file://" + avatarPath, holder.iv_avatar);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private static class ViewHolder {
        ImageView iv_avatar;
        TextView screenName;
        TextView messagePreview;
        TextView chatTime;
        TextView chatExtra;
        RelativeLayout mainLayout;
    }
}
