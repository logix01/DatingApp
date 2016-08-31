package com.enuke.blinder.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.enuke.blinder.Entity.UserMatchEntity;
import com.enuke.blinder.R;
import com.enuke.blinder.activity.NavigationActivity;
import com.enuke.blinder.adapter.MyMatchesAdapter;
import com.enuke.blinder.database.DBHelper;
import com.enuke.blinder.database.UserTable;

import java.util.ArrayList;

/**
 * Created by nitesh on 1/22/15.
 */
public class ViewMatchesFragment extends Fragment implements View.OnClickListener {

    private Button mViewMoreButton;
    private ListView mViewMatchesListView;

    private DBHelper db;
    private MyMatchesAdapter mAllViewedMatchesAdapter;
    private ArrayList<UserMatchEntity> mAllViewedMatchesList = new ArrayList<UserMatchEntity>();

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static boolean GO_TO_CARDS = false;

    public static ViewMatchesFragment newInstance(int sectionNumber) {
        ViewMatchesFragment fragment = new ViewMatchesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewmatches, container, false);
        db = new DBHelper(getActivity());

        getActivity().getActionBar().setTitle("Viewed Matches");
        ((NavigationActivity) getActivity()).updateMatchesNotifications(0);

        registerViews(view);
        registerListeners();

        // Get all viewed matches
        new GetAllViewedMatchesSaved().execute();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((NavigationActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_more_matches:
                viewTodaysMatches();
                break;
        }
    }

    private void registerViews(View view) {
        mViewMatchesListView = (ListView) view.findViewById(R.id.view_matches_listview);
        mViewMoreButton = (Button) view.findViewById(R.id.view_more_matches);
    }

    private void registerListeners() {
        mViewMoreButton.setOnClickListener(this);
    }

    private void viewTodaysMatches() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, new TodaysMatchesFragment())
                .addToBackStack(null)
                .commit();
    }

    class GetAllViewedMatchesSaved extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mAllViewedMatchesList = UserTable.getInstance().getAllViewedMatches(getActivity(), "OPENED");
            System.out.println("no of matches saved: " + mAllViewedMatchesList.size());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAllViewedMatchesAdapter = new MyMatchesAdapter(getActivity(), mAllViewedMatchesList);
            mViewMatchesListView.setAdapter(mAllViewedMatchesAdapter);
            mAllViewedMatchesAdapter.notifyDataSetChanged();

            if(mAllViewedMatchesList.size() <= 0 && !GO_TO_CARDS) {
                GO_TO_CARDS = true;
                viewTodaysMatches();
            } else if(GO_TO_CARDS) {
                GO_TO_CARDS = false;
            }
        }
    }
}
