package com.enuke.blinder.server;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by nitesh on 1/3/15.
 */
public class ConnectServerTask extends AsyncTask<Void, Void, String> {

    private Context mContext;
    private String mUrl = "";
    private VolleyJsonObjectTask.Callback mCallback;
    private JSONObject mParams;

    public ConnectServerTask(Context context, String url, JSONObject params, VolleyJsonObjectTask.Callback callBack) {
        this.mContext = context;
        this.mUrl = url;
        this.mParams = params;
        this.mCallback = callBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        System.out.println("server url: " + mUrl);
        System.out.println("server params: " + mParams);
        String responseString = "";
        int code = 0;
        try {
            URL url = new URL(mUrl);
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url.toURI());

            // Prepare JSON to send by setting the entity
            httpPost.setEntity(new StringEntity(mParams.toString(), "UTF-8"));

            // Set up the header types needed to properly transfer JSON
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept-Encoding", "application/json");
            httpPost.setHeader("Accept-Language", "en-US");

            // Execute POST
            HttpResponse response = httpClient.execute(httpPost);
            responseString = EntityUtils.toString(response.getEntity());
            code = response.getStatusLine().getStatusCode();
            System.out.println("response: " + responseString);
            System.out.println("response code: " + code);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mCallback.callFailed(e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mCallback.callFailed(e);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mCallback.callFailed(e);
        }

        if(code == 200) {
            return responseString;
        } else {
            return "fail";
        }
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        try {
            if(response.equalsIgnoreCase("fail")) {
                mCallback.callFailed(new Exception());
            } else {
                mCallback.callSuccess(new JSONObject(response));
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }
}
