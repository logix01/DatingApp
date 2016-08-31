package com.enuke.blinder.activity;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.enuke.blinder.Entity.EducationEntity;
import com.enuke.blinder.Entity.WorkEntity;
import com.enuke.blinder.R;
import com.enuke.blinder.Utils.Constants;
import com.enuke.blinder.Utils.DownloadFromServer;
import com.enuke.blinder.Utils.Utility;
import com.enuke.blinder.adapter.ProfileDetailAdapter;
import com.enuke.blinder.database.DBHelper;
import com.enuke.blinder.database.EducationTable;
import com.enuke.blinder.database.UserMatchTable;
import com.enuke.blinder.database.WorkTable;
import com.enuke.blinder.server.ConnectServerTask;
import com.enuke.blinder.server.UpdateProfileViewed;
import com.enuke.blinder.server.VolleyJsonObjectTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.xabber.android.data.Application;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nitesh on 12/31/14.
 */
public class FullProfileActivity extends ActionBarActivity implements DownloadFromServer.IDownloadComplete, View.OnClickListener {

    private ImageView mProfileAvatar, mProfileShortlist, mProfileAudioIcon;
    private Button mSayHiButton, mWriteHiButton, mListenAudio;
    private TextView mProfileNameTV, mProfileDetailTV, mProfileSingleWordTV, mAudioAvailableTitle, mRecordTimerEnd;
    private ListView mProfileDetailListView;
    private ProgressBar mShortlistProgress, mAudioProgress;

    private String mAvatarName = "", mProfileName = "", mProfileDetail = "", mProfileSingleWord = "", mAudioUrl;
    private String mAudioTempFile, mUserId, mMatchId, mIsShortlisted, mIsViewed, mIsMessageSent, mIsXmppChatStarted;
    private DBHelper db;
    private ArrayList<WorkEntity> allWorkList = new ArrayList<WorkEntity>();
    private ArrayList<EducationEntity> allEducationList = new ArrayList<EducationEntity>();
    private ProfileDetailAdapter mProfileDetailAdapter;
    private boolean mIsPlaying = false;

    private Button mPlayButton;
    private TextView mPlayTimerEnd;
    private ImageView mMicProgress, mMicImage;
    private CountDownTimer mCountDowntimer;
    private MediaRecorder mRecorder = null;
    private boolean mPlayingRecording = false;

    private static String mRecordFile = Environment.getExternalStorageDirectory() + "/Blinder/Audio/demoChatAudio.3gpp";

    /**
     * Broadcast receiver for downloaded files
     */
    private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null) return;
            if(action.equals("download_complete")) {
                String message = intent.getStringExtra("message");
                if(message.equalsIgnoreCase("audio complete")) {
                    mIsPlaying = false;
                    if(mPlayingRecording) {
                        mPlayButton.setText("Play");
                        MediaPlayer mp = MediaPlayer.create(FullProfileActivity.this, Uri.parse(mRecordFile));
                        int duration = mp.getDuration();
                        mRecordTimerEnd.setText(Utility.timeFromMilliToString(duration));
                        recordingAnimation(false);
                    } else {
                        MediaPlayer mp = MediaPlayer.create(FullProfileActivity.this, Uri.parse(mAudioTempFile));
                        int duration = mp.getDuration();
                        mPlayTimerEnd.setText(Utility.timeFromMilliToString(duration));
                    }

//                    mListenAudio.setText("Listen");
//                    mProfileAudioIcon.setImageResource(R.drawable.audio_play_yellow);
                } else if(message.equalsIgnoreCase("download complete")) {
                    mIsPlaying = false;
                    mListenAudio.setVisibility(View.VISIBLE);
                    mAudioProgress.setVisibility(View.GONE);
                    mPlayButton.setText("Play");
                    MediaPlayer mp = MediaPlayer.create(FullProfileActivity.this, Uri.parse(mAudioTempFile));
                    int duration = mp.getDuration();
                    mPlayTimerEnd.setText(Utility.timeFromMilliToString(duration));
//                    mListenAudio.setText("Listen");
//                    mProfileAudioIcon.setImageResource(R.drawable.audio_play_yellow);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_profile);
        db = new DBHelper(FullProfileActivity.this);

        registerViews();
        getIntentValues();
        registerListeners();
        showCustomActionBar();

        new GetWorkDetailTask().execute();
        new UpdateProfileViewed(FullProfileActivity.this, mUserId, mMatchId).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        Application.getInstance().onActivityResumed(this);
        // Register download complete receiver
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction("download_complete");
        FullProfileActivity.this.registerReceiver(mDownloadReceiver, filter1);
    }

    @Override
    public void onPause() {
        super.onPause();
        Utility.stopAudio(FullProfileActivity.this);
        Application.getInstance().onActivityPaused(this);
        FullProfileActivity.this.unregisterReceiver(mDownloadReceiver);
    }

    @Override
     public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.full_profile_shortlist:
                shortlistProfile();
                break;
            case R.id.full_profile_say_hi_button:
                Utility.sayHiButtonClicked(FullProfileActivity.this, mUserId, mRecordFile, mMatchId);
                break;
            case R.id.full_profile_write_hi_button:
                Utility.writeHiButtonClicked(FullProfileActivity.this, mUserId, mMatchId);
                break;
            case R.id.full_profile_listen_audio:
//                showPlayingAlertDialog();
                Intent playIntent = new Intent(FullProfileActivity.this, RecordPlayDialogActivity.class);
                playIntent.putExtra(RecordPlayDialogActivity.FILE_PATH, mAudioTempFile);
                playIntent.putExtra(RecordPlayDialogActivity.MESSAGE_TYPE, RecordPlayDialogActivity.PLAY_INTRO);
                playIntent.putExtra(RecordPlayDialogActivity.TITLE, Constants.LISTEN_AUDIO_INTRODUCTION);
                startActivity(playIntent);
                break;
        }
    }

    @Override
    public void fileDownloaded(String filePath) {
        mAudioTempFile = filePath;
        mAudioProgress.setVisibility(View.GONE);
        mListenAudio.setVisibility(View.VISIBLE);
    }

    private void showCustomActionBar() {
        ActionBar bar = getSupportActionBar();
        BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.preferences_tab));
        bar.setBackgroundDrawable(background);
        bar.setTitle(mProfileName);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
    }

    private void getIntentValues() {
        mUserId = getIntent().getStringExtra("profile_user_id");
        mMatchId = getIntent().getStringExtra("profile_match_id");
        mAvatarName = getIntent().getStringExtra("profile_avatar_name");
        mProfileName = getIntent().getStringExtra("profile_name");
        mProfileDetail = getIntent().getStringExtra("profile_detail");
        mProfileSingleWord = getIntent().getStringExtra("profile_single_word");
        mAudioUrl = getIntent().getStringExtra("profile_audio_url");
        mAudioTempFile = getIntent().getStringExtra("profile_audio_temp_file");
        mIsShortlisted = getIntent().getStringExtra("profile_is_shortlisted");
        mIsViewed = getIntent().getStringExtra("profile_is_viewed");
        mIsMessageSent = getIntent().getStringExtra("profile_is_message_sent");
        mIsXmppChatStarted = getIntent().getStringExtra("profile_is_xmpp_chat_started");

        if(mIsShortlisted.equalsIgnoreCase("1")) {
            mProfileShortlist.setImageResource(R.drawable.shortlisted_icon);
        } else {
            mProfileShortlist.setImageResource(R.drawable.shortlisted_disable);
        }

        File audioFile = new File(mAudioTempFile);
        if(mAudioTempFile.endsWith("/") || audioFile.isDirectory()) {
            mAudioAvailableTitle.setText("Audio Not Available");
            mListenAudio.setVisibility(View.GONE);
        }

        mProfileNameTV.setText(mProfileName);
        mProfileDetailTV.setText(mProfileDetail);
        mProfileSingleWordTV.setText(mProfileSingleWord);
        ImageLoader.getInstance().displayImage("file://" + Utility.getApplicationStoragePath() + mAvatarName, mProfileAvatar);
    }

    private void registerViews() {
        mProfileAvatar = (ImageView) findViewById(R.id.full_profile_avatar);
        mProfileShortlist = (ImageView) findViewById(R.id.full_profile_shortlist);
        mProfileAudioIcon = (ImageView) findViewById(R.id.full_profile_audio_icon);
        mSayHiButton = (Button) findViewById(R.id.full_profile_say_hi_button);
        mWriteHiButton = (Button) findViewById(R.id.full_profile_write_hi_button);
        mListenAudio = (Button) findViewById(R.id.full_profile_listen_audio);
        mProfileNameTV = (TextView) findViewById(R.id.full_profile_name);
        mProfileDetailTV = (TextView) findViewById(R.id.full_profile_detail);
        mProfileSingleWordTV = (TextView) findViewById(R.id.full_profile_single_word);
        mShortlistProgress = (ProgressBar) findViewById(R.id.full_profile_shortlist_progress);
        mAudioProgress = (ProgressBar) findViewById(R.id.full_profile_audio_progress);
        mAudioAvailableTitle = (TextView) findViewById(R.id.audio_available_title);
        mProfileDetailListView = (ListView) findViewById(R.id.full_profile_detail_list);
    }

    private void registerListeners() {
        mProfileShortlist.setOnClickListener(this);
        mSayHiButton.setOnClickListener(this);
        mWriteHiButton.setOnClickListener(this);
        mListenAudio.setOnClickListener(this);
    }

    private void shortlistProfile() {
        if(com.xabber.android.ui.register.Utility.isNetworkAvailable(FullProfileActivity.this)) {
            mProfileShortlist.setVisibility(View.INVISIBLE);
            mShortlistProgress.setVisibility(View.VISIBLE);
            if(mIsShortlisted.equalsIgnoreCase("1")) {
                callShortlistAPI("0");
            } else {
                callShortlistAPI("1");
            }
        } else {
            Toast.makeText(FullProfileActivity.this, Constants.NOT_CONNECTED, Toast.LENGTH_SHORT).show();
        }
    }

    private void audioClicked() {
        mPlayingRecording = false;
        if(mIsPlaying) {
            mIsPlaying = false;
            Utility.stopAudio(FullProfileActivity.this);
//            mListenAudio.setText("Listen");
//            mProfileAudioIcon.setImageResource(R.drawable.audio_play_yellow);
            MediaPlayer mp = MediaPlayer.create(FullProfileActivity.this, Uri.parse(mAudioTempFile));
            int duration = mp.getDuration();
            mPlayTimerEnd.setText(Utility.timeFromMilliToString(duration));
            mCountDowntimer.cancel();
            recordingAnimation(false);
        } else {
            mIsPlaying = true;
            File audioFile = new File(mAudioTempFile);
            if(audioFile.exists()) {
                MediaPlayer mp = MediaPlayer.create(FullProfileActivity.this, Uri.parse(mAudioTempFile));
                int duration = mp.getDuration();
                mCountDowntimer = new CountDownTimer(duration, 1000) {
                    public void onTick(long millisUntilFinished) {
                        String v = String.format("%02d", millisUntilFinished/60000);
                        int seconds = (int)( (millisUntilFinished%60000)/1000);
                        mPlayTimerEnd.setText(String.format("%02d",seconds));
                    }

                    public void onFinish() {
//                Utility.showCustomToast(getActivity(), "Maximum limit reached");
                        mPlayTimerEnd.setText("00");
                        recordingAnimation(false);
                    }};
                mCountDowntimer.start();
                // Play Audio
//                mProfileAudioIcon.setImageResource(R.drawable.audio_stop_yellow);
//                mListenAudio.setText("Stop");
                recordingAnimation(true);
                Utility.playAudio(FullProfileActivity.this, mAudioTempFile);
            } else {
                // Download Audio
                if(com.xabber.android.ui.register.Utility.isNetworkAvailable(FullProfileActivity.this)) {
//                    mAudioProgress.setVisibility(View.VISIBLE);
//                    mListenAudio.setVisibility(View.GONE);
                    mPlayButton.setText("Downloading...");
                    Utility.downloadAudio(FullProfileActivity.this, mAudioTempFile);
                } else {
                    Toast.makeText(FullProfileActivity.this, Constants.NOT_CONNECTED, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void recordingAnimation(boolean start) {
        if(start) {
            if (mMicProgress != null) {
                mMicProgress.setVisibility(View.VISIBLE);
                AnimationDrawable frameAnimation = (AnimationDrawable) mMicProgress.getDrawable();
                frameAnimation.setCallback(mMicProgress);
                frameAnimation.setVisible(true, true);
            }
        } else {
            mMicImage.setVisibility(View.VISIBLE);
            mMicProgress.setVisibility(View.GONE);
        }
    }

    public void sendBroadcast(String matchId, String isShortlisted) {
        Intent shortlistIntent = new Intent("shortlist_complete");
        shortlistIntent.putExtra("isShortlist", mIsShortlisted);
        sendBroadcast(shortlistIntent);

        Intent localIntent = new Intent("profile_shortlisted");
        localIntent.putExtra("match_id", matchId);
        localIntent.putExtra("is_shortlisted", isShortlisted);
        sendBroadcast(localIntent);
    }

    private void callShortlistAPI(final String isShorted) {
        JSONObject finalJson = new JSONObject();
        JSONObject paramJson = new JSONObject();
        try {
            paramJson.put("match_id", mMatchId);
            paramJson.put("is_shorted", isShorted);

            finalJson.put("params", paramJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ConnectServerTask connectServerTask = new ConnectServerTask(FullProfileActivity.this, Constants.BASE_URL + Constants.UPDATE_SHORTED, finalJson, new VolleyJsonObjectTask.Callback() {

            @Override
            public void callSuccess(JSONObject result) {
                super.callSuccess(result);
                try {
                    int status = result.getInt(Constants.SERVER_STATUS);
                    System.out.println("shortlist response: " + result.toString());
                    if(status == 1) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DBHelper.USER_COLUMN_ID, mUserId);
                        contentValues.put(DBHelper.USER_COLUMN_MATCHID, mMatchId);
                        contentValues.put(DBHelper.USER_COLUMN_SHORTLISTED, isShorted);

                        UserMatchTable.getInstance().updateShortlistedUserRecord(mUserId, contentValues);

                        mShortlistProgress.setVisibility(View.GONE);
                        mProfileShortlist.setVisibility(View.VISIBLE);

                        if(mIsShortlisted.equalsIgnoreCase("1")) {
                            // Un shortlisted
                            mIsShortlisted = "0";
                            mProfileShortlist.setImageResource(R.drawable.shortlisted_disable);
                        } else {
                            // Shortlisted
                            mIsShortlisted = "1";
                            mProfileShortlist.setImageResource(R.drawable.shortlisted_icon);
                        }

                        // Send broadcast to previous screen
                        sendBroadcast(mMatchId, mIsShortlisted);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean callFailed(Exception e) {
                System.out.println("today failed");
                mShortlistProgress.setVisibility(View.GONE);
                mProfileShortlist.setVisibility(View.VISIBLE);
                mProfileShortlist.setImageResource(R.drawable.shortlisted_disable);
                return super.callFailed(e);
            }
        });
        connectServerTask.execute();
    }

    private void callViewedApi() {
        JSONObject finalJson = new JSONObject();
        JSONObject paramJson = new JSONObject();
        try {
            paramJson.put("match_id", mMatchId);

            finalJson.put("params", paramJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ConnectServerTask connectServerTask = new ConnectServerTask(FullProfileActivity.this, Constants.BASE_URL + Constants.UPDATE_VIEW, finalJson, new VolleyJsonObjectTask.Callback() {

            @Override
            public void callSuccess(JSONObject result) {
                super.callSuccess(result);
                try {
                    int status = result.getInt(Constants.SERVER_STATUS);
                    System.out.println("viewed response: " + result.toString());
                    if(status == 1) {
                        mIsViewed = "1";
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DBHelper.USER_COLUMN_ID, mUserId);
                        contentValues.put(DBHelper.USER_COLUMN_MATCHID, mMatchId);
                        contentValues.put(DBHelper.USER_COLUMN_VIEWED, mIsViewed);

                        UserMatchTable.getInstance().updateShortlistedUserRecord(mUserId, contentValues);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean callFailed(Exception e) {
                System.out.println("today failed");
                return super.callFailed(e);
            }
        });
        connectServerTask.execute();
    }

    class GetWorkDetailTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            allWorkList = WorkTable.getInstance().getAllWorkOfUser(mUserId);
            allEducationList = EducationTable.getInstance().getAllEducationOfUser(mUserId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mProfileDetailAdapter = new ProfileDetailAdapter(FullProfileActivity.this, allWorkList, allEducationList);
            mProfileDetailListView.setAdapter(mProfileDetailAdapter);
            mProfileDetailAdapter.notifyDataSetChanged();
        }
    }

}
