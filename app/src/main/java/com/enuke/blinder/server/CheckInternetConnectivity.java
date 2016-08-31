package com.enuke.blinder.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.enuke.blinder.Utils.Constants;
import com.enuke.blinder.Utils.Utility;

import org.json.JSONObject;

public class CheckInternetConnectivity extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) { 
		if (intent.getExtras() != null) {
        final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        
        if (ni != null && ni.isConnectedOrConnecting()) {
            String userUpdated = Utility.getDataFromSharedPrefs(context, Constants.USER_UPDATED);
            if(userUpdated.equalsIgnoreCase("false")) {
                Toast.makeText(context, Constants.UPDATING_PROFILE_MESSAGE, Toast.LENGTH_LONG).show();
                JSONObject params = Utility.createFinalUserJson(context);
                Utility.registerUser(context, params);
            }

            Intent shortlistIntent = new Intent("internet_check");
            shortlistIntent.putExtra("internet", "connected");
            context.sendBroadcast(shortlistIntent);
        } else {

            Intent shortlistIntent = new Intent("internet_check");
            shortlistIntent.putExtra("internet", "disconnected");
            context.sendBroadcast(shortlistIntent);
        }
    }
}
	
}