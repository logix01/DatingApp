package com.enuke.blinder.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enuke.blinder.R;
import com.enuke.blinder.Utils.Constants;
import com.enuke.blinder.Utils.Utility;
import com.enuke.blinder.database.DBHelper;
import com.enuke.blinder.database.UserMatchTable;
import com.enuke.blinder.database.UserTable;
import com.enuke.blinder.server.UpdateProfileViewed;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by nitesh on 2/9/15.
 */
public class RecordPlayDialogActivity extends Activity {

    private TextView mDialogTitle, mTimer, mTimerSuffix, mBottomText;
    private Button mCancelButton, mHoldAndTalkButton, mPlayButton, mSaveButton, mRetakeButton;
    private ImageView mMicImage, mMicProgress;
    private LinearLayout mAfterRecordLayout;

    private String mMessageType = "", mTitle = "";
    private boolean mIsPlaying = false;
    private String mFilePath;
    private MediaRecorder mRecorder = null;
    private CountDownTimer mCountDowntimer;
    private long mTotalTime = 31000;
    private String mOtherUser = "", mMatchId = "";
    private DBHelper db;

    public static final String FILE_PATH = "file_path";
    public static final String MESSAGE_TYPE = "message_type";
    public static final String TITLE = "title";
    public static final String RECORD_INTRO = "record_intro";
    public static final String RECORD_CHAT = "record_chat";
    public static final String RECORD_CHAT_FIRST_MESSAGE = "record_chat_first_message";
    public static final String RECORD_CHAT_FIRST_RESPONSE = "record_chat_first_response";
    public static final String PLAY_SELF_INTRO = "play_self_intro";
    public static final String PLAY_INTRO = "play_intro";
    public static final String OTHER_USER = "other_user";
    public static final String OTHER_USER_MATCHID = "other_user_matchid";

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
                    mPlayButton.setText("Play");
                    mTimer.setText(Utility.timeFromMilliToString(getDuration()));
                    recordingAnimation(false);
                } else if(message.equalsIgnoreCase("download complete")) {
                    mPlayButton.setText("Play");
                    mPlayButton.setEnabled(true);
                    mTimer.setVisibility(View.VISIBLE);
                    mTimerSuffix.setVisibility(View.VISIBLE);
                    mTimer.setText(Utility.timeFromMilliToString(getDuration()));
//                    Utility.closeProgress();
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_record_intro);
        db = new DBHelper(RecordPlayDialogActivity.this);

        getIntentValues();
        registerViews();
        registerListeners();
        updateView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register download complete receiver
        IntentFilter filterDownload = new IntentFilter();
        filterDownload.addAction("download_complete");
        registerReceiver(mDownloadReceiver, filterDownload);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mDownloadReceiver);
        Utility.stopAudio(RecordPlayDialogActivity.this);

        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void getIntentValues() {
        Intent intent = getIntent();
        mFilePath = intent.getStringExtra(FILE_PATH);
        mMessageType = intent.getStringExtra(MESSAGE_TYPE);
        mTitle = intent.getStringExtra(TITLE);
        mOtherUser = intent.getStringExtra(OTHER_USER);
        mMatchId = intent.getStringExtra(OTHER_USER_MATCHID);
    }

    private int getDuration() {
        try {
            MediaPlayer mp = MediaPlayer.create(RecordPlayDialogActivity.this, Uri.parse(mFilePath));
            int duration = mp.getDuration();
            mp.release();
            return duration;
        } catch (Exception e) {
            e.printStackTrace();
            new File(mFilePath).delete();
            return 0;
        }
    }

    private void updateView() {
        if(mMessageType.equalsIgnoreCase(RECORD_INTRO)) {
            File audioFile = new File(mFilePath);
            if(audioFile.exists()) {
                if(audioFile.isFile()) {
                    mAfterRecordLayout.setVisibility(View.VISIBLE);
                    mHoldAndTalkButton.setVisibility(View.GONE);
                    mSaveButton.setVisibility(View.GONE);
                    mTimer.setText(Utility.timeFromMilliToString(getDuration()));
                }
            } else {
                mAfterRecordLayout.setVisibility(View.GONE);
                mHoldAndTalkButton.setVisibility(View.VISIBLE);
            }
        } else if(mMessageType.equalsIgnoreCase(RECORD_CHAT) ||
                mMessageType.equalsIgnoreCase(RECORD_CHAT_FIRST_MESSAGE) ||
                mMessageType.equalsIgnoreCase(RECORD_CHAT_FIRST_RESPONSE)) {
            mSaveButton.setText("Send");
            mBottomText.setVisibility(View.GONE);
        } else if(mMessageType.equalsIgnoreCase(PLAY_SELF_INTRO)) {
            mHoldAndTalkButton.setVisibility(View.GONE);
            mAfterRecordLayout.setVisibility(View.VISIBLE);
            mSaveButton.setVisibility(View.GONE);
        } else if(mMessageType.equalsIgnoreCase(PLAY_INTRO)) {
            mHoldAndTalkButton.setVisibility(View.GONE);
            mAfterRecordLayout.setVisibility(View.VISIBLE);
            mSaveButton.setVisibility(View.GONE);
            mRetakeButton.setVisibility(View.GONE);
            mBottomText.setVisibility(View.GONE);
            if(new File(mFilePath).exists()) {
                mTimer.setText(Utility.timeFromMilliToString(getDuration()));
            } else {
                mTimer.setVisibility(View.INVISIBLE);
                mTimerSuffix.setVisibility(View.INVISIBLE);
                mPlayButton.setText("Download");
            }
        }
    }

    private void registerViews() {
        mHoldAndTalkButton = (Button) findViewById(R.id.record_button_intro);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mPlayButton = (Button) findViewById(R.id.dialog_play_button);
        mRetakeButton = (Button) findViewById(R.id.retake_button);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mAfterRecordLayout = (LinearLayout)  findViewById(R.id.after_record_layout);
        mTimer = (TextView) findViewById(R.id.dialog_record_timer_end);
        mTimerSuffix = (TextView) findViewById(R.id.dialog_record_timer_suffix);
        mDialogTitle = (TextView) findViewById(R.id.record_dialog_title);
        mBottomText = (TextView) findViewById(R.id.record_audio_bottom_text);
        mMicImage = (ImageView) findViewById(R.id.mic_image);
      //  mMicProgress = (ImageView) findViewById(R.id.mic_progress);
        mMicProgress = (ImageView) findViewById(R.id.mic_progress_intro);


        mDialogTitle.setText(mTitle);
    }

    private void registerListeners() {

        mHoldAndTalkButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Start recording
                        onRecord(true);
                        recordingAnimation(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        // Stop recording
                        onRecord(false);
                        recordingAnimation(false);
                        String duration = Utility.timeFromMilliToString(getDuration());
                        if(!duration.equalsIgnoreCase("00")) {
                            mAfterRecordLayout.setVisibility(View.VISIBLE);
                            mSaveButton.setVisibility(View.VISIBLE);
                            mHoldAndTalkButton.setVisibility(View.GONE);
                            mTimer.setText(Utility.timeFromMilliToString(getDuration()));
                        }
                        break;
                }
                return false;
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClicked();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMessageType.equalsIgnoreCase(RECORD_INTRO)) {
                    finish();
                } else if(mMessageType.equalsIgnoreCase(RECORD_CHAT) ||
                        mMessageType.equalsIgnoreCase(RECORD_CHAT_FIRST_MESSAGE) ||
                        mMessageType.equalsIgnoreCase(RECORD_CHAT_FIRST_RESPONSE)) {
                    Utility.makeFriend(RecordPlayDialogActivity.this, mMatchId);
                    new UpdateProfileViewed(RecordPlayDialogActivity.this, mOtherUser, mMatchId).execute();
                    File file = new File(mFilePath);
                    File toFile = new File(Utility.getApplicationAudioStoragePath() + "_00_" + Utility.timeFromMilliToString(getDuration()) + "_" + Calendar.getInstance().getTimeInMillis() + ".3gpp");
                    file.renameTo(toFile);

                    UserMatchTable.getInstance().updateUserMessageSentFlag(RecordPlayDialogActivity.this, mOtherUser);
                    String isDummy = UserTable.getInstance().getIsDummyUser(mOtherUser);
                    Utility.sendChatAudio(RecordPlayDialogActivity.this, mOtherUser, toFile.getPath());

                    if(mMessageType.equalsIgnoreCase(RECORD_CHAT_FIRST_MESSAGE)) {
                        // Update user's message sent flag
                        if(isDummy.equalsIgnoreCase("1")) {
                            String dummyMessage = Constants.DUMMY_USER_FIRST_MESSAGE_PREFIX + mMatchId + Constants.DUMMY_USER_FIRST_MESSAGE_SUFFIX;
                            Utility.sendChatText(RecordPlayDialogActivity.this, mOtherUser, dummyMessage);
                        }
                    } else if(mMessageType.equalsIgnoreCase(RECORD_CHAT_FIRST_RESPONSE)) {
                        // Update user's xmpp chat started flag
                        Utility.updateXmppChatStartedFlag(RecordPlayDialogActivity.this, mMatchId, "1");
                        UserMatchTable.getInstance().updateUserXmppChatStartedFlag(RecordPlayDialogActivity.this, mOtherUser);
                    }
                    finish();
                }
            }
        });

        mRetakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(mFilePath);
                if(file.exists()) {
                    file.delete();
                }
                Utility.stopAudio(RecordPlayDialogActivity.this);
                mAfterRecordLayout.setVisibility(View.GONE);
                mHoldAndTalkButton.setVisibility(View.VISIBLE);
                mTimer.setText("30");
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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



    /**
     * This method handles recording
     * @param start
     */
    private void onRecord(boolean start) {
        File file = new File(Utility.getApplicationStoragePath());
        if(!(file.exists())) {
            file.mkdirs();
        }
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    /**
     * Start recording
     */
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecorder.start();

        mCountDowntimer = new CountDownTimer(mTotalTime, 1000) {
            public void onTick(long millisUntilFinished) {
                String v = String.format("%02d", millisUntilFinished/60000);
                int seconds = (int)( (millisUntilFinished%60000)/1000);
                mTimer.setText(String.format("%02d",seconds));
            }

            public void onFinish() {
                recordingAnimation(false);
                stopRecording();
            }};
        mCountDowntimer.start();
    }

    /**
     * Stop recording
     */
    private void stopRecording() {
        if(mRecorder != null) {
            try {
                mRecorder.stop();
            } catch(RuntimeException e) {
                new File(mFilePath).delete();  //you must delete the outputfile when the recorder stop failed.
            } finally {
                mRecorder.release();
                mRecorder = null;

                mCountDowntimer.cancel();
            }


        }
    }

    private void playClicked() {
        if(mIsPlaying) {
            mIsPlaying = false;
            Utility.stopAudio(RecordPlayDialogActivity.this);
            mPlayButton.setText("Play");
            mTimer.setText(Utility.timeFromMilliToString(getDuration()));
            mCountDowntimer.cancel();
            recordingAnimation(false);
        } else {
            File audioFile = new File(mFilePath);
            if(audioFile.exists()) {
                mIsPlaying = true;
                mCountDowntimer = new CountDownTimer(getDuration(), 1000) {
                    public void onTick(long millisUntilFinished) {
                        String v = String.format("%02d", millisUntilFinished/60000);
                        int seconds = (int)( (millisUntilFinished%60000)/1000);
                        mTimer.setText(String.format("%02d",seconds));
                    }

                    public void onFinish() {
                        mTimer.setText("00");
                        recordingAnimation(false);
                        stopRecording();
                    }};
                mCountDowntimer.start();
                // Play Audio
                mPlayButton.setText("Stop");
                recordingAnimation(true);
                Utility.playAudio(RecordPlayDialogActivity.this, mFilePath);
            } else {
                // Download Audio
                if(com.xabber.android.ui.register.Utility.isNetworkAvailable(RecordPlayDialogActivity.this)) {
                    String url = Constants.AUDIO_DOWNLOAD_URL + Utility.getFileNameFromUrl(mFilePath);
//                    Utility.showProgress(RecordPlayDialogActivity.this, "Downloading...");
                    mPlayButton.setText("Downloading...");
                    mPlayButton.setEnabled(false);
                    Utility.downloadAudio(RecordPlayDialogActivity.this, url);
                } else {
                    Toast.makeText(RecordPlayDialogActivity.this, Constants.NOT_CONNECTED, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
