package com.enuke.blinder.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.enuke.blinder.R;
import com.enuke.blinder.Utils.Utility;
import com.enuke.blinder.activity.NavigationActivity;

/**
 * Created by nitesh on 12/31/14.
 */
public class RateUsFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static RateUsFragment newInstance(int sectionNumber) {
        RateUsFragment fragment = new RateUsFragment();
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
        View view = inflater.inflate(R.layout.fragment_rateus, container, false);
        getActivity().getActionBar().setTitle("Rate Us");

        Utility.extractDatabaseFromDevice(getActivity());

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((NavigationActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
}
