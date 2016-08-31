package com.enuke.blinder.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.enuke.blinder.Entity.UserMatchEntity;
import com.enuke.blinder.R;
import com.enuke.blinder.adapter.MyMatchesAdapter;
import com.enuke.blinder.database.DBHelper;
import com.enuke.blinder.database.UserTable;

import java.util.ArrayList;

/**
 * Created by nitesh on 2/12/15.
 */
public class ViewedMatchesActivity extends ActionBarActivity implements View.OnClickListener {

    private Button mViewMoreButton;
    private ListView mViewMatchesListView;

    private DBHelper db;
    private MyMatchesAdapter mAllViewedMatchesAdapter;
    private ArrayList<UserMatchEntity> mAllViewedMatchesList = new ArrayList<UserMatchEntity>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_viewmatches);
        db = new DBHelper(ViewedMatchesActivity.this);

        registerViews();
        registerListeners();
        showCustomActionBar();

        // Get all viewed matches
        new GetAllViewedMatchesSaved().execute();
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
            case R.id.view_more_matches:
                viewTodaysMatches();
                break;
        }
    }

    private void registerViews() {
        mViewMatchesListView = (ListView) findViewById(R.id.view_matches_listview);

        View footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_viewmatches_listfooter, null, false);
        mViewMatchesListView.addFooterView(footerView);

        mViewMoreButton = (Button) footerView.findViewById(R.id.view_more_matches);
    }

    private void registerListeners() {
        mViewMoreButton.setOnClickListener(this);
    }

    private void showCustomActionBar() {
        ActionBar bar = getSupportActionBar();
        BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.preferences_tab));
        bar.setBackgroundDrawable(background);
        bar.setTitle("Viewed Matches");
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
    }

    private void viewTodaysMatches() {

        Intent todayIntent = new Intent(ViewedMatchesActivity.this, TodayMatchesActivity.class);
        startActivity(todayIntent);
    }

    class GetAllViewedMatchesSaved extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mAllViewedMatchesList = UserTable.getInstance().getAllViewedMatches(ViewedMatchesActivity.this, "OPENED");
            System.out.println("no of matches saved: " + mAllViewedMatchesList.size());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAllViewedMatchesAdapter = new MyMatchesAdapter(ViewedMatchesActivity.this, mAllViewedMatchesList);
            mViewMatchesListView.setAdapter(mAllViewedMatchesAdapter);
            mAllViewedMatchesAdapter.notifyDataSetChanged();

            if(mAllViewedMatchesList.size() <= 0) {
                viewTodaysMatches();
            }
        }
    }
}