package com.enuke.blinder.Utils;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

/**
 * Created by nitesh on 1/12/15.
 */
public class PlayAudioService extends Service {
    private static final String LOGCAT = null;
    MediaPlayer objPlayer;

    public void onCreate(){
        super.onCreate();
        Log.d(LOGCAT, "Service Started!");
        objPlayer = new MediaPlayer();
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        String path = intent.getStringExtra("audioPath");
        try {
            objPlayer.reset();
            objPlayer.setDataSource(path);
            objPlayer.prepare();
            objPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopSelf();
                    sendBroadcast("audio complete");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        objPlayer.start();
        Log.d(LOGCAT, "Media Player started!");
        if(objPlayer.isLooping() != true){
            Log.d(LOGCAT, "Problem in Playing Audio");
        }
        return 1;
    }

    public void onStop(){
        objPlayer.stop();
        objPlayer.release();
    }

    public void onPause(){
        objPlayer.stop();
        objPlayer.release();
    }
    public void onDestroy(){
        objPlayer.stop();
        objPlayer.release();
    }

    @Override
    public IBinder onBind(Intent objIndent) {
        return null;
    }

    public void sendBroadcast(String extraMessage) {
        Intent localIntent = new Intent("download_complete");
        localIntent.putExtra("message", extraMessage);
        sendBroadcast(localIntent);
    }
}
