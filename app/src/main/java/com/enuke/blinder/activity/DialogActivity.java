package com.enuke.blinder.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.enuke.blinder.R;
import com.enuke.blinder.Utils.Constants;

/**
 * Created by nitesh on 1/29/15.
 */
public class DialogActivity extends Activity {

    private TextView mDialogMessage, mTitle;
    private Button mOkButton, mCancelButton, mCancelBottomButton;
    private String mNotificationType, mNotificationMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_new);
//        getActionBar().hide();

        getIntentValues();
        registerViews();
        registerListeners();
    }

    private void getIntentValues() {
        mNotificationType = getIntent().getStringExtra("NotificationType");
        mNotificationMessage = getIntent().getStringExtra("NotificationMessage");
    }

    private void registerViews() {
        mDialogMessage = (TextView) findViewById(R.id.dialog_message);
        mOkButton = (Button) findViewById(R.id.dialog_ok_button);
        mCancelButton = (Button) findViewById(R.id.dialog_cancel_button);
        mCancelBottomButton = (Button) findViewById(R.id.dialog_cancel_bottom_button);
        mTitle = (TextView) findViewById(R.id.dialog_title);

        mDialogMessage.setText(mNotificationMessage);
        mTitle.setText(Constants.NOTIFICATION_TITLE);
        mCancelBottomButton.setVisibility(View.GONE);
    }

    private void registerListeners() {
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DialogActivity.this, NavigationActivity.class);
                if(mNotificationType.equalsIgnoreCase("OPENED")) {
                    intent.putExtra(Constants.OPEN_THIS_CLASS, Constants.OPEN_VIEW_MATCHES);
                } else if(mNotificationType.equalsIgnoreCase("MATCHED")) {
                    intent.putExtra(Constants.OPEN_THIS_CLASS, Constants.OPEN_DAILY_MATCHES);
                }
                startActivity(intent);
                finish();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
