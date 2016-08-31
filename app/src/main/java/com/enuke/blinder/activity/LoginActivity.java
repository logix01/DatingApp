package com.enuke.blinder.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.enuke.blinder.R;
import com.enuke.blinder.Utils.Constants;
import com.enuke.blinder.Utils.Utility;
import com.enuke.blinder.database.DBHelper;
import com.enuke.blinder.database.UserTable;
import com.enuke.blinder.server.ConnectServerTask;
import com.enuke.blinder.server.VolleyJsonObjectTask;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.xabber.android.data.Application;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.intent.AccountIntentBuilder;
import com.xabber.android.ui.register.Constansts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nitesh on 12/24/14.
 */
public class LoginActivity extends ActionBarActivity {

    private final String TAG = "LoginActivity";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private LoginButton loginBtn;
//    private Button postImageBtn;
//    private Button updateStatusBtn;

    private TextView userName;

    private UiLifecycleHelper uiHelper;

    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");

    private static String message = "Sample status posted from android app";
    private String mRecordFile = Environment.getExternalStorageDirectory() + "/Blinder/IntroductionAudio.3gpp";

    String SENDER_ID = "";

    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, statusCallback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        context = getApplicationContext();
        SENDER_ID = getResources().getString(R.string.project_number);

        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        File file = new File(mRecordFile);
        if(file.exists()) {
            file.delete();

        }

        userName = (TextView) findViewById(R.id.user_name);
        loginBtn = (LoginButton) findViewById(R.id.fb_login_button);
        loginBtn.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday",
                "user_hometown", "user_location", "user_work_history", "user_education_history"));
        loginBtn.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                if (user != null) {
                    Utility.showProgress(LoginActivity.this, Constants.PLEASE_WAIT_MESSAGE);


                    Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.FACEBOOK_EMAILID, user.getProperty("email").toString());

                    Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.FACEBOOK_LOGIN, "true");

                    String fbjson=user.getInnerJSONObject().toString();

                    Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.FACEBOOK_JSON, user.getInnerJSONObject().toString());

                    System.out.println("fbjson: " + user.getInnerJSONObject().toString());
                    String dob = user.getBirthday();

                    int  diiff=0;
                    // 1988-02-28
                    if (dob!=null){
                          diiff= Utility.getDifferenceYears(Calendar.getInstance(), Utility.getCalendarFromStringNew(dob));

                        System.out.println(diiff);
                    }


                    GraphPlace gp = user.getLocation();
                    if(dob != null && diiff>= 15)
                        Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.DATE_OF_BIRTH, dob);

                    if(gp != null) {
                        if(gp.getName() != null)
                            Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.LOCATION, gp.getName());
                    }
                    checkUserRegistration(user.getProperty("email").toString());
//                    Intent myProfileIntent = new Intent(LoginActivity.this, HomeActivity.class);
//                    startActivity(myProfileIntent);
//                    finish();
                } else {
//                    userName.setText("You are not logged");
                }
            }
        });

        buttonsEnabled(false);
    }

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            if (state.isOpened()) {
                buttonsEnabled(true);
                Log.d("FacebookSampleActivity", "Facebook session opened");
            } else if (state.isClosed()) {
                buttonsEnabled(false);
                Log.d("FacebookSampleActivity", "Facebook session closed");
            }
        }
    };

    public void buttonsEnabled(boolean isEnabled) {
//        postImageBtn.setEnabled(isEnabled);
//        updateStatusBtn.setEnabled(isEnabled);
    }

    public void postImage() {
        if (checkPermissions()) {
            Bitmap img = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_launcher);
            Request uploadRequest = Request.newUploadPhotoRequest(
                    Session.getActiveSession(), img, new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            Toast.makeText(LoginActivity.this,
                                    "Photo uploaded successfully",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
            uploadRequest.executeAsync();
        } else {
            requestPermissions();
        }
    }

    public void postStatusMessage() {
        if (checkPermissions()) {
            Request request = Request.newStatusUpdateRequest(
                    Session.getActiveSession(), message,
                    new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {
                            if (response.getError() == null)
                                Toast.makeText(LoginActivity.this,
                                        "Status updated successfully",
                                        Toast.LENGTH_LONG).show();
                        }
                    });
            request.executeAsync();
        } else {
            requestPermissions();
        }
    }

    public boolean checkPermissions() {
        Session s = Session.getActiveSession();
        if (s != null) {
            return s.getPermissions().contains("publish_actions");
        } else
            return false;
    }

    public void requestPermissions() {
        Session s = Session.getActiveSession();
        if (s != null)
            s.requestNewPublishPermissions(new Session.NewPermissionsRequest(
                    this, PERMISSIONS));
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        buttonsEnabled(Session.getActiveSession().isOpened());
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        uiHelper.onSaveInstanceState(savedState);
    }



    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode !=  ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        final String PREFS_NAME = "BlinderPreferences";
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
//                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                System.out.println("regId: " + o.toString());
            }
        }.execute(null, null, null);
    }

    private void checkUserRegistration(String username) {
        JSONObject finalJson = new JSONObject();
        JSONObject paramJson = new JSONObject();
        try {
            paramJson.put("username", username);
            paramJson.put("gcm_id", Utility.getDataFromSharedPrefs(LoginActivity.this, HomeActivity.PROPERTY_REG_ID));
            paramJson.put("device_id", Utility.getDeviceKey(LoginActivity.this));
            paramJson.put("device_type", "Android");

            finalJson.put("params", paramJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ConnectServerTask connectServerTask = new ConnectServerTask(LoginActivity.this, Constants.BASE_URL + Constants.GET_USER_PROFILE, finalJson, new VolleyJsonObjectTask.Callback() {

            @Override
            public void callSuccess(JSONObject response) {
                super.callSuccess(response);
                try {
                    int status = response.getInt(Constants.SERVER_STATUS);
                    System.out.println("register response: " + response.toString());
                    if(status == 1) {
                        // User is registered
                        JSONObject userJson = response.getJSONArray("data").getJSONObject(0);
//                        registerUser(userJson);

                        Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.USER_REGISTERED, "true");
                        String mUserId = userJson.getString(Constants.USER_ID_KEY);
                        String xmppPassword = "";
                        if(response.has(Constants.XMPP_PASSWORD))
                            xmppPassword= response.getString(Constants.XMPP_PASSWORD);
                        com.xabber.android.ui.register.Utility.SaveToSharedPreferences(LoginActivity.this, Constansts.Phone_number, mUserId);
                        Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.USER_ID_KEY, mUserId);
                        Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.XMPP_USERNAME, mUserId + Constants.XMPP_ID_SUFFIX);
                        Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.XMPP_PASSWORD, xmppPassword);

                        insertUserInDatabase(userJson);
                        String xmppLogin = Utility.getDataFromSharedPrefs(LoginActivity.this, Constants.XMPP_LOGIN);
                        if(TextUtils.isEmpty(xmppLogin) || !xmppLogin.equalsIgnoreCase("true"))
                            xmppAutoLogin();

                        Utility.closeProgress();

                        Intent navIntent = new Intent(LoginActivity.this, NavigationActivity.class);
                        startActivity(navIntent);
                        finish();

                    } else if(status == 0) {
                        // User is not registered
                        Intent myProfileIntent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(myProfileIntent);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean callFailed(Exception e) {
                System.out.println("register failed");
                // User is not registered
                Intent myProfileIntent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(myProfileIntent);
                finish();
                return super.callFailed(e);
            }
        });
        connectServerTask.execute();
    }

    /**
     * Create JSON to send to server while registration
     * @return
     */
    private JSONObject createFinalUserJson(JSONObject response) {
        JSONObject finalJson = new JSONObject();
        JSONObject paramJson = new JSONObject();
        try {
            paramJson.put("describe_id", response.getString("describe_id"));
            paramJson.put("max_age_intrested_in", response.getString("max_age_intrested_in"));
            paramJson.put("user_id", response.getString("user_id"));
            paramJson.put("gcm_id", Utility.getDataFromSharedPrefs(LoginActivity.this, HomeActivity.PROPERTY_REG_ID));
            paramJson.put("dob", response.getString("dob"));
            paramJson.put("is_intrested_in", response.getString("is_intrested_in"));
            paramJson.put("audio_path", response.getString("audio_path"));
            paramJson.put("min_age_intrested_in", response.getString("min_age_intrested_in"));
            paramJson.put("fbJson", Utility.getDataFromSharedPrefs(LoginActivity.this, Constants.FACEBOOK_JSON));
            paramJson.put("location", response.getString("location"));
            paramJson.put("latitude", response.getString("latitude"));
            paramJson.put("longitude", response.getString("longitude"));
            paramJson.put("avtar_code", response.getString("avtar_code"));
            paramJson.put("device_id", Utility.getDeviceKey(LoginActivity.this));
            paramJson.put("device_type", "Android");
            paramJson.put("gender", response.getString("gender"));
            paramJson.put("screen_name", response.getString("screen_name"));
            paramJson.put("username", response.getString("username"));

            finalJson.put("params", paramJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return finalJson;
    }

/*    private void registerUser(final JSONObject params) {
        System.out.println("reg params: " + params.toString());
        VolleyJsonObjectTask volleyJsonObjectTask = new VolleyJsonObjectTask(LoginActivity.this, Constants.BASE_URL + Constants.REGISTER_USER, params, new VolleyJsonObjectTask.Callback() {

            @Override
            public void callSuccess(JSONObject result) {
                super.callSuccess(result);
                try {
                    int status = result.getInt(Constants.SERVER_STATUS);
                    // {"xmpp_password":"0c8bf4f1a2","status":1,"user_id":1}
                    System.out.println("registration response: " + result.toString());
                    if(status == 1) {
                        Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.USER_REGISTERED, "true");
                        String mUserId = String.valueOf(result.getInt(Constants.USER_ID_KEY));
                        String xmppPassword = "";
                        if(result.has(Constants.XMPP_PASSWORD))
                            xmppPassword= result.getString(Constants.XMPP_PASSWORD);
                        com.xabber.android.ui.register.Utility.SaveToSharedPreferences(LoginActivity.this, Constansts.Phone_number, mUserId);
                        Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.USER_ID_KEY, mUserId);
                        Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.XMPP_USERNAME, mUserId + Constants.XMPP_ID_SUFFIX);
                        Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.XMPP_PASSWORD, xmppPassword);

                        insertUserInDatabase(params);
                        String xmppLogin = Utility.getDataFromSharedPrefs(LoginActivity.this, Constants.XMPP_LOGIN);
                        if(TextUtils.isEmpty(xmppLogin) || !xmppLogin.equalsIgnoreCase("true"))
                            xmppAutoLogin();

                        Utility.closeProgress();

                        Intent navIntent = new Intent(LoginActivity.this, NavigationActivity.class);
                        startActivity(navIntent);
                        finish();
                    } else {
                        Utility.closeProgress();
                        Toast.makeText(LoginActivity.this, Constants.REGISTER_FAIL_MESSAGE, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Utility.closeProgress();
                    e.printStackTrace();
                }
            }

            @Override
            public boolean callFailed(Exception e) {
                System.out.println("registration failed");
                return super.callFailed(e);
            }
        });
        volleyJsonObjectTask.execute();
    }*/

    private void insertUserInDatabase(JSONObject user) {
        DBHelper db = new DBHelper(LoginActivity.this);
        try {
            String dob = user.getString("dob");
            String[] dobArray = dob.split("-");
            String dobString = "";
            for(int i=(dobArray.length - 1); i>=0; i--) {
                if(i!=0)
                    dobString += dobArray[i] + ":";
                else
                    dobString += dobArray[i];
            }
            System.out.println("date of birth: " + dobString);
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.USER_COLUMN_ID, user.getString("user_id"));
            contentValues.put(DBHelper.USER_COLUMN_NAME, user.getString("username"));
            contentValues.put(DBHelper.USER_COLUMN_SCREEN_NAME, user.getString("screen_name"));
            contentValues.put(DBHelper.USER_COLUMN_DEVICE_ID, user.getString("device_id"));
            contentValues.put(DBHelper.USER_COLUMN_GENDER, user.getString("gender"));
            contentValues.put(DBHelper.USER_COLUMN_ONEWORD, user.getString("describe_id"));
            contentValues.put(DBHelper.USER_COLUMN_DOB, dobString);
            contentValues.put(DBHelper.USER_COLUMN_LOCATION, user.getString("location"));
            contentValues.put(DBHelper.USER_COLUMN_LATITUDE, user.getString("latitude"));
            contentValues.put(DBHelper.USER_COLUMN_LONGITUDE, user.getString("longitude"));
            contentValues.put(DBHelper.USER_COLUMN_AUDIOPATH, user.getString("audio_path"));
            contentValues.put(DBHelper.USER_COLUMN_AVATAR_CODE, user.getString("avtar_code"));
            contentValues.put(DBHelper.USER_COLUMN_INTERESTED_IN_MALE, (user.getString("is_intrested_in").equalsIgnoreCase("male") || user.getString("is_intrested_in").equalsIgnoreCase("both"))? "true" : "false");
            contentValues.put(DBHelper.USER_COLUMN_INTERESTED_IN_FEMALE, (user.getString("is_intrested_in").equalsIgnoreCase("female") || user.getString("is_intrested_in").equalsIgnoreCase("both"))? "true" : "false");


            contentValues.put(DBHelper.USER_COLUMN_MIN_AGE_INTERESTED_IN, user.getString("min_age_intrested_in"));
            contentValues.put(DBHelper.USER_COLUMN_MAX_AGE_INTERESTED_IN, user.getString("max_age_intrested_in"));

            UserTable.getInstance().updateUserRecord(user.getString("user_id"), contentValues);

        } catch (Exception excpetion) {
            excpetion.printStackTrace();
        }
    }

    /**
     * This methods logs in in xmpp automatically and
     * sets profile pic as selected avatar
     */
    private void xmppAutoLogin() {
        System.out.println("xmpp auto login");
        final String account;
        String username = Utility.getDataFromSharedPrefs(LoginActivity.this, Constants.XMPP_USERNAME);
        String password = Utility.getDataFromSharedPrefs(LoginActivity.this, Constants.XMPP_PASSWORD);
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

        createAuthenticatorResult(LoginActivity.this, account);
        com.xabber.android.ui.register.Utility.SaveToSharedPreferences(LoginActivity.this, "Account", account);

        com.xabber.android.ui.register.Utility.SaveToSharedPreferences(LoginActivity.this, Constansts.FIRST_SCREEN, Constansts.show_list);

        Utility.saveDataTosharedPrefs(LoginActivity.this, Constants.XMPP_LOGIN, "true");

    }

    private static Intent createAuthenticatorResult(Context context, String account) {
        return new AccountIntentBuilder(null, null).setAccount(account).build();
    }
}
