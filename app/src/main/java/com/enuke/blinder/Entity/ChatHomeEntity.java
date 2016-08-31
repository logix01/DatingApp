package com.enuke.blinder.Entity;

/**
 * Created by nitesh on 1/20/15.
 */
public class ChatHomeEntity {

    private String userid;
    private String avatarcode;
    private String screenname;
    private String latestmessage;
    private String time;
    private String extraMessage;
    private String read;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLatestmessage() {
        return latestmessage;
    }

    public void setLatestmessage(String latestmessage) {
        this.latestmessage = latestmessage;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getAvatarcode() {
        return avatarcode;
    }

    public void setAvatarcode(String avatarcode) {
        this.avatarcode = avatarcode;
    }

    public String getScreenname() {
        return screenname;
    }

    public void setScreenname(String screenname) {
        this.screenname = screenname;
    }

    public String getExtraMessage() {
        return extraMessage;
    }

    public void setExtraMessage(String extraMessage) {
        this.extraMessage = extraMessage;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }
}
