package com.enuke.blinder.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.enuke.blinder.R;
import com.enuke.blinder.Utils.Constants;
import com.enuke.blinder.Utils.Utility;
import com.enuke.blinder.activity.DialogActivity;
import com.enuke.blinder.activity.NavigationActivity;
import com.enuke.blinder.receiver.GcmMessageBroadcastReceiver;
import com.google.android.gms.gcm.GoogleCloudMessaging;
/**
 * Created by nitesh on 1/2/15.
 */

public class GcmMessageIntentService extends IntentService {
    private final String TAG = "GcmIntentService";
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    String notificationMode;

    public GcmMessageIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */

            Log.i("noti extra string",""+extras.toString());
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), "NOTIFICATION");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString(), "NOTIFICATION");
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Handling notifications
//                JSONObject json = new JSONObject();
//                Set<String> keys = bundle.keySet();
//                for (String key : keys) {
//                    try {
//                        json.put(key, (bundle.get(key)));
//                    } catch(JSONException e) {
//                    }
//                }
                //Received: Bundle[{from=1021671695478, mode=OPENED, matches=7, message=Hi! good girl opened your card, like to communicate with him?, android.support.content.wakelockid=2, collapse_key=do_not_collapse}]
                Log.i(TAG, "Received: " + extras.toString());
                String message = "";
                // vishal
                try {


                    notificationMode = extras.getString("mode");

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if(notificationMode !=null) {
                    if (notificationMode.equalsIgnoreCase("OPENED")) {
                        //Update match notification count
                        int count = 0;
                        String matchCount = Utility.getDataFromSharedPrefs(this, Constants.MATCH_NOTIFICATION_COUNT);
                        if (!TextUtils.isEmpty(matchCount)) {
                            count = Integer.parseInt(matchCount);
                        } else {
                            count = 0;
                        }
                        count++;
                        Utility.saveDataTosharedPrefs(this.getApplicationContext(), Constants.MATCH_NOTIFICATION_COUNT, String.valueOf(count));

                        message = extras.getString("message");
                        String matchId = extras.getString("matches");
                        Utility.cardOpenedNotification(this.getApplicationContext(), matchId);
                        // Post notification of received message.
                        if (Utility.isApplicationRunning.equalsIgnoreCase("true")) {
                            Intent intentDialog = new Intent(this, DialogActivity.class);
                            intentDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intentDialog.putExtra("NotificationType", notificationMode);
                            intentDialog.putExtra("NotificationMessage", message);
                            startActivity(intentDialog);
                        } else {
                            sendNotification(message, Constants.OPEN_VIEW_MATCHES);
                        }
                    } else if (notificationMode.equalsIgnoreCase("MATCHED")) {
                        message = Constants.MATCH_NOTIFICATION_MESSAGE;
                        Utility.callTodayMatchesApi(this.getApplicationContext());
                        // Post notification of received message.
                        if (Utility.isApplicationRunning.equalsIgnoreCase("true")) {
                            Intent intentDialog = new Intent(this, DialogActivity.class);
                            intentDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intentDialog.putExtra("NotificationType", notificationMode);
                            intentDialog.putExtra("NotificationMessage", message);
                            startActivity(intentDialog);
                        } else {
                            sendNotification(message, Constants.OPEN_DAILY_MATCHES);
                        }
                    }
                }

                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmMessageBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, String openThisClass) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification sound
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(this, NavigationActivity.class);
        if(openThisClass.equalsIgnoreCase(Constants.OPEN_VIEW_MATCHES)) {
            intent.putExtra(Constants.OPEN_THIS_CLASS, Constants.OPEN_VIEW_MATCHES);
        } else if(openThisClass.equalsIgnoreCase(Constants.OPEN_DAILY_MATCHES)) {
            intent.putExtra(Constants.OPEN_THIS_CLASS, Constants.OPEN_DAILY_MATCHES);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Blinder")
                        .setAutoCancel(true)
                        .setSound(soundUri)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setTicker(msg)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}