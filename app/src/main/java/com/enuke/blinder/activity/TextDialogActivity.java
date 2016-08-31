package com.enuke.blinder.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.enuke.blinder.R;
import com.enuke.blinder.Utils.Constants;
import com.enuke.blinder.Utils.Utility;
import com.enuke.blinder.database.DBHelper;
import com.enuke.blinder.database.UserMatchTable;
import com.enuke.blinder.database.UserTable;
import com.enuke.blinder.server.UpdateProfileViewed;

/**
 * Created by nitesh on 2/9/15.
 */
public class TextDialogActivity extends Activity {

    private EditText mEditTextMessage;
    private Button mSendButton, mCancelButton;
    private String mOtherUser = "", mMatchId = "", mMessageType = "";

    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_text);
        db = new DBHelper(TextDialogActivity.this);

        getIntentValues();
        registerViews();
        registerListeners();
    }

    private void getIntentValues() {
        Intent intent = getIntent();
        mOtherUser = intent.getStringExtra(RecordPlayDialogActivity.OTHER_USER);
        mMatchId = intent.getStringExtra(RecordPlayDialogActivity.OTHER_USER_MATCHID);
        mMessageType = intent.getStringExtra(RecordPlayDialogActivity.MESSAGE_TYPE);
    }

    private void registerViews() {
        mEditTextMessage = (EditText) findViewById(R.id.editext_message);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mSendButton = (Button) findViewById(R.id.send_button);
    }

    private void registerListeners() {

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utility.hideKeyboard(TextDialogActivity.this, mCancelButton);
                finish();
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mEditTextMessage.getText().toString().trim();
                if(message.length() > 0) {
                    Utility.makeFriend(TextDialogActivity.this, mMatchId);
                    new UpdateProfileViewed(TextDialogActivity.this, mOtherUser, mMatchId).execute();
                    Utility.sendChatText(TextDialogActivity.this, mOtherUser, message);
                    Utility.hideKeyboard(TextDialogActivity.this, mCancelButton);

                    if(mMessageType.equalsIgnoreCase(RecordPlayDialogActivity.RECORD_CHAT_FIRST_MESSAGE)) {
                        // Update user's message sent flag
                        UserMatchTable.getInstance().updateUserMessageSentFlag(TextDialogActivity.this, mOtherUser);
                        String isDummy = UserTable.getInstance().getIsDummyUser(mOtherUser);
                        if(isDummy.equalsIgnoreCase("1")) {
                            String dummyMessage = Constants.DUMMY_USER_FIRST_MESSAGE_PREFIX + mMatchId + Constants.DUMMY_USER_FIRST_MESSAGE_SUFFIX;
                            Utility.sendChatText(TextDialogActivity.this, mOtherUser, dummyMessage);
                        }
                    } else if(mMessageType.equalsIgnoreCase(RecordPlayDialogActivity.RECORD_CHAT_FIRST_RESPONSE)) {
                        // Update user's xmpp chat started flag
                        Utility.updateXmppChatStartedFlag(TextDialogActivity.this, mMatchId, "1");
                        UserMatchTable.getInstance().updateUserXmppChatStartedFlag(TextDialogActivity.this, mOtherUser);
                    }
                    finish();
                }
            }
        });
    }
}
