package com.enuke.blinder.Utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.enuke.blinder.Entity.UserEntity;
import com.enuke.blinder.Entity.UserMatchEntity;
import com.enuke.blinder.R;
import com.enuke.blinder.activity.HomeActivity;
import com.enuke.blinder.activity.RecordPlayDialogActivity;
import com.enuke.blinder.activity.TextDialogActivity;
import com.enuke.blinder.activity.TodayMatchesActivity;
import com.enuke.blinder.custom_menuitem.BadgeDrawable;
import com.enuke.blinder.database.AvatarTable;
import com.enuke.blinder.database.DBHelper;
import com.enuke.blinder.database.UserMatchTable;
import com.enuke.blinder.database.UserTable;
import com.enuke.blinder.fragment.MyProfileFragment;
import com.enuke.blinder.server.ConnectServerTask;
import com.enuke.blinder.server.VolleyJsonObjectTask;
import com.xabber.android.data.Application;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.connection.ConnectionManager;
import com.xabber.android.data.intent.AccountIntentBuilder;
import com.xabber.android.data.message.MessageManager;
import com.xabber.android.data.message.MessageTable;
import com.xabber.android.ui.ChatViewer;
import com.xabber.android.ui.register.Constansts;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by nitesh on 12/29/14.
 */
public class Utility {

    public static MediaRecorder mRecorder = null;
    public static MediaPlayer mPlayer = null;
    private static ProgressDialog mProgress;
    public static String isApplicationRunning = "false";
    private static int result_status;


    /**
     * This method saves the data in shared preferences
     * @param mContext
     * @param key
     * @param data
     */

    public static void saveDataTosharedPrefs(Context mcontext , String key , String data){

        final String PREFS_NAME = "BlinderPreferences";
        final SharedPreferences blinderAppData = mcontext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = blinderAppData.edit();
        editor.putString(key,data);
        editor.commit();

}
    /**
     * This method gets data from shared preferences
     * @param mContext
     * @param key
     * @return
     */
    public static String getDataFromSharedPrefs(Context mContext, String key) {
        final String PREFS_NAME = "BlinderPreferences";
        final SharedPreferences blinderAppData = mContext.getSharedPreferences(PREFS_NAME, 0);
        final String preData = blinderAppData.getString(key, "").trim();
        return preData;
    }

    /**
     * This method gets the device key
     * @param mContext
     * @return
     */
    public static String getDeviceKey(Context mContext) {
        String theDevId;
        if (Utility.getDataFromSharedPrefs(mContext, Constants.DEVICE_ID).length() > 0) {
            theDevId = Utility.getDataFromSharedPrefs(mContext, Constants.DEVICE_ID);

        } else {

            theDevId = ((TelephonyManager) mContext
                    .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if (theDevId == null || theDevId == "" || theDevId == " "
                    || theDevId == "0") {

                try {
                    theDevId = Settings.Secure.getString(mContext.getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                } catch (Exception e) {
                    theDevId = String.valueOf(Calendar.getInstance()
                            .getTimeInMillis());
                    System.out.println("Error while getting Device Id");
                }
            }
        }
        return theDevId;

    }

    public static String getApplicationStoragePath() {
        String path = Environment.getExternalStorageDirectory() + "/Blinder/";
        File file = new File(path);
        if(!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String getApplicationAudioStoragePath() {
        String audioPath = Environment.getExternalStorageDirectory() + "/Blinder/Audio/";
        File file = new File(audioPath);
        if(!file.exists()) {
            file.mkdirs();
        }
        return audioPath;
    }

    public static void downloadAudio(Context context, String audioPath) {
        ArrayList<String> downloadLinks = new ArrayList<String>();
        downloadLinks.add(audioPath);
        Intent i = new Intent(context, DownloadFromServer.class);
        i.putExtra("downloadlinks", downloadLinks);
        context.startService(i);
    }

    public static void playAudio(Context context, String path) {
        Intent playIntent = new Intent(context, PlayAudioService.class);
        playIntent.putExtra("audioPath", path);
        context.startService(playIntent);
    }

    public static void stopAudio(Context context) {
        Intent objIntent = new Intent(context, PlayAudioService.class);
        context.stopService(objIntent);
    }

    public static String getImageName(String completeName) {
        return completeName.substring(completeName.indexOf("/") + 1);
    }

    public static String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1).replace("%20", " ");
    }

    public static ProgressDialog showProgress(Context context, CharSequence message) {
        return showProgress(context, message, false, null);
    }

    public static ProgressDialog showProgress(Context context, CharSequence message, boolean cancelable, DialogInterface.OnCancelListener listener) {
        mProgress = new ProgressDialog(context);
        mProgress.setMessage(message);
        mProgress.setIndeterminate(false);
        mProgress.setCancelable(cancelable);
        mProgress.setOnCancelListener(listener);
        mProgress.show();
        return mProgress;
    }

    public static void closeProgress() {
        try {
            if (mProgress != null) {
                mProgress.dismiss();
                mProgress = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showLongToast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show();
    }

    public static void showShortToast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static void showCustomToast(Context context, String message) {
        Toast customToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        customToast.setGravity(Gravity.TOP, 0, 0);
        customToast.show();
    }

    public static void setBadgeCount(Context context, LayerDrawable icon, int count) {
        BadgeDrawable badge;
        // Reuse drawable if possible
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_badge);
        if (reuse != null && reuse instanceof BadgeDrawable) {
            badge = (BadgeDrawable) reuse;
        } else {
            badge = new BadgeDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_badge, badge);
    }

    /**
     * This method handles recording
     * @param start
     */
    public static void onRecord(boolean start, String path) {
        File file = new File(Utility.getApplicationStoragePath());
        if(!(file.exists())) {
            file.mkdirs();
        }
        if (start) {
            startRecording(path);
        } else {
            stopRecording();
        }
    }

    /**
     * Start recording
     */
    public static void startRecording(String path) {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(path);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecorder.start();
    }

    /**
     * Stop recording
     */
    public static void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    /**
     * This method handles playing the recording
     * @param filePath
     */
    public static void onPlay(String filePath) {
        if(mPlayer != null && mPlayer.isPlaying()) {
            stopPlaying();
        } else {
            startPlaying(filePath);
        }
    }

    /**
     * Start playing
     * @param filePath
     */
    public static  void startPlaying(String filePath) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(filePath);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mPlayer.release();
                    mPlayer = null;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop playing
     */
    public static void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    public static final String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return (sdf.format(cal.getTime()));
    }

    public static Calendar getCalendarFromString(String date) {
        // 1988-02-28
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal  = Calendar.getInstance();
        try {
            cal.setTime(df.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }

    public static Calendar getCalendarFromStringNew(String date) {
        // 1988-02-28
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Calendar cal  = Calendar.getInstance();
        try {
            cal.setTime(df.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }

    public static int getDifferenceYears(Calendar a, Calendar b) {
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return Math.abs(diff);
    }

    public static int getDifferenceDays(Calendar a, Calendar b) {
        long diff = a.getTimeInMillis() - b.getTimeInMillis();
        diff = Math.abs(diff);
        int days = (int) (diff / (24 * 60 * 60 * 1000));
        return days;
    }

    public static boolean isDateAfter(Calendar a, Calendar b) {
        return a.after(b);
    }

    public static void extractDatabaseFromDevice(Context context) {
        File sd = Environment.getExternalStorageDirectory();
        String DB_PATH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DB_PATH = context.getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
        }
        else {
            DB_PATH = context.getFilesDir().getPath() + context.getPackageName() + "/databases/";
        }
        if (sd.canWrite()) {
            //Blinder DB
            String currentDBPath = "Blinder.db";
            String backupDBPath = "blinder_backupname.db";
            File currentDB = new File(DB_PATH, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (currentDB.exists()) {
                try {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            //Xabber DB
            String xabberDBPath = "xabber.db";
            String xabberbackupDBPath = "xabber_backupname.db";
            File xabberDB = new File(DB_PATH, xabberDBPath);
            File xabberbackupDB = new File(sd, xabberbackupDBPath);

            if (currentDB.exists()) {
                try {
                    FileChannel src = new FileInputStream(xabberDB).getChannel();
                    FileChannel dst = new FileOutputStream(xabberbackupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void startChatting(Context context, String other, String bottomArea) {
        String me = com.xabber.android.ui.register.Utility.getAccount(context);
        System.out.println("me: " + me + " other: " + other);

        String visibility = bottomArea;
        int count = 0;
        int counter = 0;
        Cursor cursor = MessageTable.getInstance().getMessagesList(me, other + Constants.XMPP_ID_SUFFIX);
        try {
            if (cursor.moveToFirst()) {
                do {
                    count = cursor.getInt(0);
                    String incoming = cursor.getString(1);
                    if(count == 0) {
//                        visibility = bottomArea;
                    } else if(count == 1 && incoming.equalsIgnoreCase("0")) {
//                        visibility = "hide";
                    } else if(count == 1 && incoming.equalsIgnoreCase("1")) {
                        counter++;
//                        visibility = "show";
                    } else {
//                        visibility = "show";
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        Intent i= ChatViewer.createIntent(context, me, other + Constants.XMPP_ID_SUFFIX);
        i.putExtra("messagecount", visibility);
        i.putExtra("counter", counter);
        i.putExtra("otherUserId", other);
        context.startActivity(i);
    }

    public static String hiClicked(Context context, String other) {
        String message = "";
        String me = com.xabber.android.ui.register.Utility.getAccount(context);
        Cursor cursor = MessageTable.getInstance().getMessagesList(me, other + Constants.XMPP_ID_SUFFIX);
        int count = 0;
        try {
            if (cursor.moveToFirst()) {
                do {
                    count = cursor.getInt(0);
                    String incoming = cursor.getString(1);
                    if(count == 0) {
                        message = "true";
                    } /*else if(count == 1 && incoming.equalsIgnoreCase("0")) {
                        message = "false";
                    } else if(count == 1 && incoming.equalsIgnoreCase("1")) {

                    }*/ else {
                        message = "false";
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            if(count == 0) {
                message = "true";
            } else {
                message = "false";
            }
            cursor.close();
        }
        return message;
    }

    public static String chatBottomLayout(String account, String user) {
        String visibility = "unknown";
        Cursor cursor = MessageTable.getInstance().getMessagesList(account, user);
        try {
            if (cursor.moveToFirst()) {
                do {
                    int count = cursor.getInt(0);
                    String incoming = cursor.getString(1);
                    if(count == 0) {
                        visibility = "unknown";
                    } else if(count == 1 && incoming.equalsIgnoreCase("0")) {
                        visibility = "hide";
                    } else {
                        visibility = "show";
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return visibility;
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, 0);
    }

    public static String timeFromMilliToString(int milli) {
        int seconds = (int) Math.ceil(milli / 1000);
        String time = "";
        if(seconds >= 60) {
            time = String.format("%02d", seconds%60) + ":" + String.format("%02d", seconds/60);
        } else {
            time = String.format("%02d", seconds);
        }
        return time;
    }

	/**//**
     * @param bitmap
     * @return converting bitmap and return a string
     *//*
*/	public static byte[] BitMapToString(Bitmap bitmap) {

        Bitmap bb = null;
        bb = Bitmap.createScaledBitmap(bitmap, 80, 80, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bb.compress(Bitmap.CompressFormat.PNG, 50, baos);

        byte[] b = baos.toByteArray();
        //String temp = Base64.encodeToString(b, Base64.DEFAULT);
        // System.out.println(temp);
        return b;
    }

    public static String changeDobFormat(String oldDate, String oldFormat, String newFormat) {
        DateFormat originalFormat = new SimpleDateFormat(oldFormat);
        DateFormat targetFormat = new SimpleDateFormat(newFormat);
        Date date = null;
        try {
            date = originalFormat.parse(oldDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);
        return formattedDate;
    }

    public static void setAvatar(final String userId, Bitmap bitmap, final Context context) {
//        AvatarStorage.getInstance().write(userId, com.xabber.android.ui.register.Utility.ByteFromImage(bitmap));
        byte[] base64 = null;
        if (bitmap!=null) {
            base64 = BitMapToString(bitmap);
        }
        final byte[] finalBase6 = base64;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                XMPPConnection con= null ;
                try {
                    con = ConnectionManager.getInstance().getConnection(userId);
                } catch (NetworkException e) {
                    e.printStackTrace();
                }

                try {
                    org.jivesoftware.smackx.packet.VCard vCard = new org.jivesoftware.smackx.packet.VCard();
                    // vCard.load(con);
                    vCard.setAvatar(finalBase6, "image/jpg");
                    //vCard.setNickName(str_name);

                    vCard.save(con, context);
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public static String getDate(long timeStamp){
        DateFormat objFormatter = new SimpleDateFormat("dd MMM, yyyy");

        Calendar objCalendar = Calendar.getInstance();
        objCalendar.setTimeInMillis(timeStamp);
        String result = objFormatter.format(objCalendar.getTime());
        objCalendar.clear();
        return result;
    }

    public static String formatTimeString(Context context, long when) {

        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        // Basic settings for formatDateTime we want for all cases.
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                DateUtils.FORMAT_ABBREV_ALL |
                DateUtils.FORMAT_CAP_AMPM |
                DateUtils.FORMAT_SHOW_TIME;

        return DateUtils.formatDateTime(context, when, format_flags);
    }

    public static void callTodayMatchesApi(Context context) {
        final DBHelper db = new DBHelper(context);
        JSONObject finalJson = new JSONObject();
        JSONObject paramJson = new JSONObject();
        try {
            paramJson.put("user_id", Utility.getDataFromSharedPrefs(context, Constants.USER_ID_KEY));

            finalJson.put("params", paramJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("params: " + finalJson);

        ConnectServerTask connectServerTask = new ConnectServerTask(context, Constants.BASE_URL + Constants.TODAY_MATCHES, finalJson, new VolleyJsonObjectTask.Callback() {

            @Override
            public void callSuccess(JSONObject result) {
                super.callSuccess(result);
                try {
                    int status = result.getInt(Constants.SERVER_STATUS);
                    System.out.println("today noti response: " + result.toString());
                    if(status == 1) {
                        JSONArray matchesArray = result.getJSONArray("data");
                        System.out.println("today noti length: " + matchesArray.length());
//                        mMaxRedHeart = matchesArray.length();
                        for(int i=0; i<matchesArray.length(); i++) {
                            JSONObject jsonObject = matchesArray.getJSONObject(i);
//                            mTodaysMatchesList.add(jsonObject);

                            ContentValues contentValues = new ContentValues();
                            ContentValues matchContentValues = new ContentValues();

                            String userId = jsonObject.getString("user_id");
                            String matchId = jsonObject.getString("match_id");;
                            String userName = jsonObject.getString("username");
                            String screenName = jsonObject.getString("screen_name");
                            String deviceId = jsonObject.getString("device_id");
                            String gender = jsonObject.getString("gender");
                            String oneWord = jsonObject.getString("describe_id");
                            String dob = jsonObject.getString("dob");
                            String location = jsonObject.getString("location");
                            String latitude = jsonObject.getString("latitude");
                            String longitude = jsonObject.getString("longitude");
                            String audioPath = jsonObject.getString("audio_path");
                            String avatarCode = jsonObject.getString("avtar_code");
                            String minAge = jsonObject.getString("min_age_intrested_in");
                            String maxAge = jsonObject.getString("max_age_intrested_in");
                            String matchHint = jsonObject.getString("hint");
                            String isDummy = jsonObject.getString("isDummy");
                            String isShorted = "0";
                            if(jsonObject.has("is_shorted")) {
                                isShorted = jsonObject.getString("is_shorted");
                            }
                            String isViewed = "0";
                            if(jsonObject.has("is_match_viewed")) {
                                isViewed = jsonObject.getString("is_match_viewed");
                            }
                            String isMessageSent = jsonObject.getString("is_msg_sent");
                            String isXmppChatStarted = jsonObject.getString("isXMPPChatStarted");

                            contentValues.put(DBHelper.USER_COLUMN_ID, userId);
                            contentValues.put(DBHelper.USER_COLUMN_NAME, userName);
                            contentValues.put(DBHelper.USER_COLUMN_SCREEN_NAME, screenName);
                            contentValues.put(DBHelper.USER_COLUMN_DEVICE_ID, deviceId);
                            contentValues.put(DBHelper.USER_COLUMN_GENDER, gender);
                            contentValues.put(DBHelper.USER_COLUMN_ONEWORD, oneWord);
                            contentValues.put(DBHelper.USER_COLUMN_DOB, dob);
                            contentValues.put(DBHelper.USER_COLUMN_LOCATION, location);
                            contentValues.put(DBHelper.USER_COLUMN_LATITUDE, latitude);
                            contentValues.put(DBHelper.USER_COLUMN_LONGITUDE, longitude);
                            contentValues.put(DBHelper.USER_COLUMN_AUDIOPATH, audioPath);
                            contentValues.put(DBHelper.USER_COLUMN_AVATAR_CODE, avatarCode);
                            contentValues.put(DBHelper.USER_COLUMN_MIN_AGE_INTERESTED_IN, minAge);
                            contentValues.put(DBHelper.USER_COLUMN_MAX_AGE_INTERESTED_IN, maxAge);
                            contentValues.put(DBHelper.USER_COLUMN_MATCH_HINT, matchHint);
                            contentValues.put(DBHelper.USER_COLUMN_DUMMY_USER, isDummy);

                            String interestedIn = jsonObject.getString("is_intrested_in");
                            if(interestedIn.equalsIgnoreCase("male")) {
                                contentValues.put(DBHelper.USER_COLUMN_INTERESTED_IN_MALE, "true");
                                contentValues.put(DBHelper.USER_COLUMN_INTERESTED_IN_FEMALE, "false");
                            } else if(interestedIn.equalsIgnoreCase("female")) {
                                contentValues.put(DBHelper.USER_COLUMN_INTERESTED_IN_MALE, "false");
                                contentValues.put(DBHelper.USER_COLUMN_INTERESTED_IN_FEMALE, "true");
                            } else {
                                contentValues.put(DBHelper.USER_COLUMN_INTERESTED_IN_MALE, "true");
                                contentValues.put(DBHelper.USER_COLUMN_INTERESTED_IN_FEMALE, "true");
                            }

                            matchContentValues.put(DBHelper.USER_COLUMN_ID, userId);
                            matchContentValues.put(DBHelper.USER_COLUMN_MATCHID, matchId);
                            matchContentValues.put(DBHelper.USER_COLUMN_SHORTLISTED, isShorted);
                            matchContentValues.put(DBHelper.USER_COLUMN_VIEWED, isViewed);
                            matchContentValues.put(DBHelper.USER_COLUMN_MESSAGE_SENT, isMessageSent);
                            matchContentValues.put(DBHelper.USER_COLUMN_XMPP_CHAT_STARTED, isXmppChatStarted);
                            matchContentValues.put(DBHelper.USER_COLUMN_FLAG, "MATCHED");
                            matchContentValues.put(DBHelper.USER_COLUMN_DATE, String.valueOf(Calendar.getInstance().getTimeInMillis()));

                            UserTable.getInstance().updateUserRecord(userId, contentValues);
                            UserMatchTable.getInstance().updateShortlistedUserRecord(userId, matchContentValues);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean callFailed(Exception e) {
                System.out.println("today noti failed");
                return super.callFailed(e);
            }
        });
        connectServerTask.execute();
    }

    public static void cardOpenedNotification(Context context, String matchId) {
        final DBHelper db = new DBHelper(context);
        // Getting user by match
        JSONObject finalJson = new JSONObject();
        JSONObject paramJson = new JSONObject();
        try {
            paramJson.put("match_id", matchId);

            finalJson.put("params", paramJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("params: " + finalJson);

        ConnectServerTask connectServerTask = new ConnectServerTask(context, Constants.BASE_URL + Constants.USER_BY_MATCH, finalJson, new VolleyJsonObjectTask.Callback() {

            @Override
            public void callSuccess(JSONObject result) {
                super.callSuccess(result);
                try {
                    int status = result.getInt(Constants.SERVER_STATUS);
                    System.out.println("update view noti response: " + result.toString());
                    if(status == 1) {
                        UserTable.getInstance().insertNewUserDetail(result.getJSONObject("data"), "OPENED");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean callFailed(Exception e) {
                System.out.println("update view noti failed");
                return super.callFailed(e);
            }
        });
        connectServerTask.execute();
    }

    /**
     * Create JSON to send to server while registration
     * @return
     */
    public static JSONObject createFinalUserJson(Context context) {
        String userId = Utility.getDataFromSharedPrefs(context, Constants.USER_ID_KEY);
        if(userId == null || TextUtils.isEmpty(userId)) {
            userId = Constants.DEFAULT_USER_ID;
        }
        DBHelper db = new DBHelper(context);
        ArrayList<UserEntity> userList = UserTable.getInstance().getUser(userId);
        UserEntity user = null;
        System.out.println("user size: " + userList.size());
        if(userList.size() > 0) {
            user = userList.get(0);
        }
        JSONObject finalJson = new JSONObject();
        JSONObject paramJson = new JSONObject();
        if(user != null) {
            try {
                paramJson.put("describe_id", user.getOneword());
                paramJson.put("max_age_intrested_in", user.getMaxage());
                paramJson.put("user_id", userId);
                paramJson.put("gcm_id", Utility.getDataFromSharedPrefs(context, HomeActivity.PROPERTY_REG_ID));


                paramJson.put("dob",user.getDob());
                paramJson.put("is_intrested_in", getInterestedIn(user.getInterestfemale(), user.getInterestmale()));
                paramJson.put("audio_path", user.getAudiopath());
                paramJson.put("min_age_intrested_in", user.getMinage());
                paramJson.put("fbJson", Utility.getDataFromSharedPrefs(context, Constants.FACEBOOK_JSON));
                paramJson.put("location", user.getLocation());
                paramJson.put("latitude", user.getLatitude());
                paramJson.put("longitude", user.getLongitude());
                paramJson.put("avtar_code", user.getAvatarcode());
                paramJson.put("device_id", Utility.getDeviceKey(context));
                paramJson.put("device_type", "Android");
                paramJson.put("gender", user.getGender());
                paramJson.put("screen_name", user.getScreenname());
                paramJson.put("username", Utility.getDataFromSharedPrefs(context, Constants.FACEBOOK_EMAILID));

                finalJson.put("params", paramJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return finalJson;
    }

    public static String getInterestedIn(String female, String male) {
        String interested = "";
        if(female != null && male != null && female.equalsIgnoreCase("true") && male.equalsIgnoreCase("true")) {
            interested = "Both";
        } else if(female != null && female.equalsIgnoreCase("true")) {
            interested = "Female";
        } else if(male != null && male.equalsIgnoreCase("true")) {
            interested = "Male";
        }
        return interested;
    }

    /**
     * Register user on server
     * @param params
     */
    public static void registerUser(final Context context, JSONObject params) {
        System.out.println("reg params: " + params.toString());
       
        
        VolleyJsonObjectTask volleyJsonObjectTask = new VolleyJsonObjectTask(context, Constants.BASE_URL + Constants.REGISTER_USER, params, new VolleyJsonObjectTask.Callback() {

           
            @Override
            public void callSuccess(JSONObject result) {
                super.callSuccess(result);
                try {
                    int status = result.getInt(Constants.SERVER_STATUS);
                    String message=result.getString("message");
                    System.out.println("registration response: " + result.toString());

                   if (status==0){
                       
                       Utility.closeProgress();

                       
                   }
                   else
                    if(status == 1) {
                        Utility.saveDataTosharedPrefs(context, Constants.USER_UPDATED, "true");
                        Utility.saveDataTosharedPrefs(context, Constants.USER_REGISTERED, "true");
                        String mUserId = String.valueOf(result.getInt(Constants.USER_ID_KEY));
                        String xmppPassword = "";
                        if(result.has(Constants.XMPP_PASSWORD))
                            xmppPassword= result.getString(Constants.XMPP_PASSWORD);
                        com.xabber.android.ui.register.Utility.SaveToSharedPreferences(context, Constansts.Phone_number, mUserId);
                        Utility.saveDataTosharedPrefs(context, Constants.USER_ID_KEY, mUserId);
                        Utility.saveDataTosharedPrefs(context, Constants.XMPP_USERNAME, mUserId + Constants.XMPP_ID_SUFFIX);
                        Utility.saveDataTosharedPrefs(context, Constants.XMPP_PASSWORD, xmppPassword);

                        if(new File(MyProfileFragment.mRecordFile).exists()) {
                            // Upload audio file
                            String sendAudioName = "audio_" + mUserId + "_" + Calendar.getInstance().getTimeInMillis() + ".3gpp";
                            System.out.println("uploading recording of user: " + mUserId);
                            Intent intent = new Intent(context, FileUpload.class);
                            intent.putExtra(Constants.USER_ID_KEY, mUserId);
                            intent.putExtra("file_path", MyProfileFragment.mRecordFile);
                            intent.putExtra("file_new_name", sendAudioName);
                            intent.putExtra("file_mime_type", "audio/3gpp");
                            context.startService(intent);
                        }

                        String xmppLogin = Utility.getDataFromSharedPrefs(context, Constants.XMPP_LOGIN);
                        if(TextUtils.isEmpty(xmppLogin) || !xmppLogin.equalsIgnoreCase("true"))
                            xmppAutoLogin(context);

                        DBHelper db = new DBHelper(context);
                        String avatarCode = UserTable.getInstance().getUserAvatarCode(mUserId);
                        String avatarName = AvatarTable.getInstance().getAvatarName(avatarCode);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(Utility.getApplicationStoragePath() + avatarName, options);
                        Utility.setAvatar(com.xabber.android.ui.register.Utility.getAccount(context), bitmap, context);

                        Utility.closeProgress();
                        showUpdatedProfile(context,"Profile","Profile Updated Successfully");
                    //    Toast.makeText(context, Constants.PROFILE_UPDATED_MESSAGE, Toast.LENGTH_LONG).show();



                        if(context instanceof HomeActivity) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DBHelper.USER_COLUMN_ID, mUserId);
                            UserTable.getInstance().updateUserRecord("0", contentValues);

//                            Intent localIntent = new Intent("register_complete");
//                            context.sendBroadcast(localIntent);
                        }


                    } else {

                        Toast.makeText(context, Constants.REGISTER_FAIL_MESSAGE, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Utility.closeProgress();
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean callFailed(Exception e) {
                System.out.println("registration failed");
                Utility.closeProgress();
                return super.callFailed(e);
            }
        });
        volleyJsonObjectTask.execute();

    }



    public static int registerUsernew(final Context context, JSONObject params) {
        System.out.println("reg params: " + params.toString());


        VolleyJsonObjectTask volleyJsonObjectTask = new VolleyJsonObjectTask(context, Constants.BASE_URL + Constants.REGISTER_USER, params, new VolleyJsonObjectTask.Callback() {


            @Override
            public void callSuccess(JSONObject result) {
                super.callSuccess(result);
                try {
                    int status = result.getInt(Constants.SERVER_STATUS);

                    System.out.println("registration response: " + result.toString());

                    if (status==0){

                        Utility.closeProgress();
                        result_status=0;
                        String message=result.getString("message");
                        Toast.makeText(context,  message, Toast.LENGTH_SHORT).show();

                    }
                    else
                    if(status == 1) {
                        Utility.saveDataTosharedPrefs(context, Constants.USER_UPDATED, "true");
                        Utility.saveDataTosharedPrefs(context, Constants.USER_REGISTERED, "true");
                        String mUserId = String.valueOf(result.getInt(Constants.USER_ID_KEY));
                        String xmppPassword = "";
                        if(result.has(Constants.XMPP_PASSWORD))
                            xmppPassword= result.getString(Constants.XMPP_PASSWORD);
                        com.xabber.android.ui.register.Utility.SaveToSharedPreferences(context, Constansts.Phone_number, mUserId);
                        Utility.saveDataTosharedPrefs(context, Constants.USER_ID_KEY, mUserId);
                        Utility.saveDataTosharedPrefs(context, Constants.XMPP_USERNAME, mUserId + Constants.XMPP_ID_SUFFIX);
                        Utility.saveDataTosharedPrefs(context, Constants.XMPP_PASSWORD, xmppPassword);

                        if(new File(MyProfileFragment.mRecordFile).exists()) {
                            // Upload audio file
                            String sendAudioName = "audio_" + mUserId + "_" + Calendar.getInstance().getTimeInMillis() + ".3gpp";
                            System.out.println("uploading recording of user: " + mUserId);
                            Intent intent = new Intent(context, FileUpload.class);
                            intent.putExtra(Constants.USER_ID_KEY, mUserId);
                            intent.putExtra("file_path", MyProfileFragment.mRecordFile);
                            intent.putExtra("file_new_name", sendAudioName);
                            intent.putExtra("file_mime_type", "audio/3gpp");
                            context.startService(intent);
                        }

                        String xmppLogin = Utility.getDataFromSharedPrefs(context, Constants.XMPP_LOGIN);
                        if(TextUtils.isEmpty(xmppLogin) || !xmppLogin.equalsIgnoreCase("true"))
                            xmppAutoLogin(context);

                        DBHelper db = new DBHelper(context);
                        String avatarCode = UserTable.getInstance().getUserAvatarCode(mUserId);
                        String avatarName = AvatarTable.getInstance().getAvatarName(avatarCode);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(Utility.getApplicationStoragePath() + avatarName, options);
                        Utility.setAvatar(com.xabber.android.ui.register.Utility.getAccount(context), bitmap, context);

                        Utility.closeProgress();
                        Toast.makeText(context, Constants.PROFILE_UPDATED_MESSAGE, Toast.LENGTH_LONG).show();

                        if(context instanceof HomeActivity) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DBHelper.USER_COLUMN_ID, mUserId);
                            UserTable.getInstance().updateUserRecord("0", contentValues);

//                            Intent localIntent = new Intent("register_complete");
//                            context.sendBroadcast(localIntent);
                        }
                        result_status=1;

                    } else {
                        result_status=3;
                        Toast.makeText(context, Constants.REGISTER_FAIL_MESSAGE, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Utility.closeProgress();
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean callFailed(Exception e) {
                System.out.println("registration failed");
                Utility.closeProgress();
                return super.callFailed(e);
            }
        });
        volleyJsonObjectTask.execute();
        return result_status;
    }

    public static void makeFriend(Context context, String matchId) {
        JSONObject finalJson = new JSONObject();
        JSONObject paramJson = new JSONObject();
        try {
            paramJson.put("match_id", matchId);

            finalJson.put("params", paramJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ConnectServerTask connectServerTask = new ConnectServerTask(context, Constants.BASE_URL + Constants.MAKE_FRIEND, finalJson, new VolleyJsonObjectTask.Callback() {

            @Override
            public void callSuccess(JSONObject result) {
                super.callSuccess(result);
                try {
                    int status = result.getInt(Constants.SERVER_STATUS);
                    System.out.println("friend response: " + result.toString());
                    if(status == 1) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean callFailed(Exception e) {
                System.out.println("friend failed");
                return super.callFailed(e);
            }
        });
        connectServerTask.execute();
    }

    /**
     * This methods logs in in xmpp automatically and
     * sets profile pic as selected avatar
     */
    public static void xmppAutoLogin(Context context) {
        System.out.println("xmpp auto login");
        final String account;
        String username = Utility.getDataFromSharedPrefs(context, Constants.XMPP_USERNAME);
        String password = Utility.getDataFromSharedPrefs(context, Constants.XMPP_PASSWORD);
        try {
            account = AccountManager.getInstance().addAccount(username,
                    password,  AccountManager.getInstance().getAccountTypes().get(0),
                    true,
                    true,
                    false);
        } catch (NetworkException e) {
            Application.getInstance().onError(e);
            return;
        }

        createAuthenticatorResult(context, account);
        com.xabber.android.ui.register.Utility.SaveToSharedPreferences(context, "Account", account);

        com.xabber.android.ui.register.Utility.SaveToSharedPreferences(context, Constansts.FIRST_SCREEN, Constansts.show_list);

        Utility.saveDataTosharedPrefs(context, Constants.XMPP_LOGIN, "true");

    }

    public static Intent createAuthenticatorResult(Context context, String account) {
        return new AccountIntentBuilder(null, null).setAccount(account).build();
    }

    public static void showMatchesDialog(Context context, String dialogTitle, String dialogMessage) {
        final Dialog dialog = new Dialog(context, R.style.customDialogTheme);
        dialog.setContentView(R.layout.activity_dialog_new);
        dialog.setCancelable(false);
        Button okButton = (Button) dialog.findViewById(R.id.dialog_ok_button);
        Button cancelButton = (Button) dialog.findViewById(R.id.dialog_cancel_button);
        Button cancelBottomButton = (Button) dialog.findViewById(R.id.dialog_cancel_bottom_button);
        TextView message = (TextView) dialog.findViewById(R.id.dialog_message);
        TextView title = (TextView) dialog.findViewById(R.id.dialog_title);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);

        message.setText(dialogMessage);
        title.setText(dialogTitle);
        cancelBottomButton.setVisibility(View.GONE);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public static void showUpdatedProfile(final Context context, String dialogTitle, String dialogMessage) {
        final Dialog dialog = new Dialog(context, R.style.customDialogTheme);
        dialog.setContentView(R.layout.activity_dialog_new);
        dialog.setCancelable(false);
        Button okButton = (Button) dialog.findViewById(R.id.dialog_ok_button);
        Button cancelButton = (Button) dialog.findViewById(R.id.dialog_cancel_button);
        Button cancelBottomButton = (Button) dialog.findViewById(R.id.dialog_cancel_bottom_button);
        TextView message = (TextView) dialog.findViewById(R.id.dialog_message);
        TextView title = (TextView) dialog.findViewById(R.id.dialog_title);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);

        message.setText(dialogMessage);
        title.setText(dialogTitle);
        cancelBottomButton.setVisibility(View.GONE);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, TodayMatchesActivity.class));
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static void writeHiButtonClicked(Context context, String userId, String matchId) {
        if(com.xabber.android.ui.register.Utility.isNetworkAvailable(context)) {
            DBHelper db = new DBHelper(context);
            UserMatchEntity user = UserTable.getInstance().getUserMatch(userId);
            // 0 means first message is not sent
            // 1 means first message is sent but response is not received yet
            String isMessageSent = user.getUserismessagesent();
            // 0 means chat is not started
            // 1 means chat is started
            String isXmppChatStarted = user.getUserisxmppchatstarted();
            if(isXmppChatStarted.equalsIgnoreCase("1")) {
                // start chatting
                writeHi(context, userId, matchId, RecordPlayDialogActivity.RECORD_CHAT);
            } else if(isMessageSent.equalsIgnoreCase("1")) {
                // Show alert
                Utility.showMatchesDialog(context, Constants.WRITE_HI_TITLE, Constants.FIRST_MESSAGE_SENT_MESSAGE);
            } else {
                String message = hiClicked(context, userId);
                if(message.equalsIgnoreCase("true")) {
                    // Send first text message
                    writeHi(context, userId, matchId, RecordPlayDialogActivity.RECORD_CHAT_FIRST_MESSAGE);
                } else {
                    // Send first text response
                    writeHi(context, userId, matchId, RecordPlayDialogActivity.RECORD_CHAT_FIRST_RESPONSE);
                }
            }
        } else {
            Utility.showMatchesDialog(context, Constants.NOT_CONNECTED_TITLE, Constants.NOT_CONNECTED);
        }
    }

    public static void writeHi(Context context, String userId, String matchId, String message) {
        Intent writeChatIntent = new Intent(context, TextDialogActivity.class);
        writeChatIntent.putExtra(RecordPlayDialogActivity.OTHER_USER, userId);
        writeChatIntent.putExtra(RecordPlayDialogActivity.OTHER_USER_MATCHID, matchId);
        writeChatIntent.putExtra(RecordPlayDialogActivity.MESSAGE_TYPE, message);
        context.startActivity(writeChatIntent);
    }

    public static void sayHiButtonClicked(Context context, String userId, String recordFile, String matchId) {
        if(com.xabber.android.ui.register.Utility.isNetworkAvailable(context)) {
            DBHelper db = new DBHelper(context);
            UserMatchEntity user = UserTable.getInstance().getUserMatch(userId);
            String isMessageSent = user.getUserismessagesent();
            String isXmppChatStarted = user.getUserisxmppchatstarted();
            if(isXmppChatStarted.equalsIgnoreCase("1")) {
                // start chatting
                sayHi(context, userId, recordFile, matchId, RecordPlayDialogActivity.RECORD_CHAT);
            } else if(isMessageSent.equalsIgnoreCase("1")) {
                // Show alert
                Utility.showMatchesDialog(context, Constants.SAY_HI_TITLE, Constants.FIRST_MESSAGE_SENT_MESSAGE);
            } else {
                String message = hiClicked(context, userId);
                if(message.equalsIgnoreCase("true")) {
                    // Send first audio message
                    sayHi(context, userId, recordFile, matchId, RecordPlayDialogActivity.RECORD_CHAT_FIRST_MESSAGE);
                } else {
                    // Send first audio response
                    sayHi(context, userId, recordFile, matchId, RecordPlayDialogActivity.RECORD_CHAT_FIRST_RESPONSE);
                }
            }
        } else {
            Utility.showMatchesDialog(context, Constants.NOT_CONNECTED_TITLE, Constants.NOT_CONNECTED);
        }
    }

    public static void sayHi(Context context, String userId, String recordFile, String matchId, String message) {
        Intent recordChatIntent = new Intent(context, RecordPlayDialogActivity.class);
        recordChatIntent.putExtra(RecordPlayDialogActivity.FILE_PATH, recordFile);
        recordChatIntent.putExtra(RecordPlayDialogActivity.MESSAGE_TYPE, message);
        recordChatIntent.putExtra(RecordPlayDialogActivity.TITLE, Constants.SAY_HI_TITLE);
        recordChatIntent.putExtra(RecordPlayDialogActivity.OTHER_USER, userId);
        recordChatIntent.putExtra(RecordPlayDialogActivity.OTHER_USER_MATCHID, matchId);
        context.startActivity(recordChatIntent);
    }

    public static void updateXmppChatStartedFlag(Context context, String matchId, String xmppChatStartedFlag) {
        JSONObject finalJson = new JSONObject();
        JSONObject paramJson = new JSONObject();
        try {
            paramJson.put("match_id", matchId);
            paramJson.put("isXMPPChatStarted", xmppChatStartedFlag);

            finalJson.put("params", paramJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ConnectServerTask connectServerTask = new ConnectServerTask(context, Constants.BASE_URL + Constants.XMPP_CHAT_STARTED, finalJson, new VolleyJsonObjectTask.Callback() {

            @Override
            public void callSuccess(JSONObject result) {
                super.callSuccess(result);
                try {
                    int status = result.getInt(Constants.SERVER_STATUS);
                    System.out.println("xmpp chat response: " + result.toString());
                    if(status == 1) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean callFailed(Exception e) {
                System.out.println("xmpp chat failed");
                return super.callFailed(e);
            }
        });
        connectServerTask.execute();
    }

    public static void sendChatText(Context context, String other, String text) {
        String me = com.xabber.android.ui.register.Utility.getAccount(context);
        MessageManager.getInstance().sendMessage(me, other + Constants.XMPP_ID_SUFFIX, text);
    }

    public static void sendChatAudio(Context context, String other, String path) {
        String finalText = "#" + Constansts.AUDIO_TAG + "audio/3gpp" + "#" + path + "#" + "null" + "#" + null + "#" + 0 + "#" + 100;
        String me = com.xabber.android.ui.register.Utility.getAccount(context);
        MessageManager.getInstance().sendAttachment(me, other + Constants.XMPP_ID_SUFFIX, finalText);
    }
}
