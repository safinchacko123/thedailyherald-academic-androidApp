package com.w9565277.thedailyherald;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class NewsListFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NewsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportNewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsListFragment newInstance(String param1, String param2) {
        NewsListFragment fragment = new NewsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_news_list, container, false);

        //Search box
        EditText searchtxt = (EditText) rootView.findViewById(R.id.searchtxt);
        searchtxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    menuSetNormal(rootView);
                    searchSetBold(searchtxt);
                    ((MainActivity) getActivity()).searchNewsbyTopic();
                    return true;
                }
                return false;
            }
        });

        //Menu Trending Default Loading
        ((MainActivity) getActivity()).loadTrendingNews();
        TextView menu_trending = (TextView) rootView.findViewById(R.id.menu_trending);
        menuSetNormal(rootView);
        menuSetBold(menu_trending);
        if (menu_trending != null) {
            menu_trending.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    searchtxt.setText("");
                    menuSetNormal(rootView);
                    menuSetBold(menu_trending);
                    //Toast.makeText(getContext(), "Trending News Loaded !", Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).loadTrendingNews();

                }
            });
        }

        //location based near me
        TextView menu_near_me = (TextView) rootView.findViewById(R.id.menu_near_me);
        if (menu_near_me != null) {
            menu_near_me.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    searchtxt.setText("");
                    menuSetNormal(rootView);
                    menuSetBold(menu_near_me);
                    ((MainActivity) getActivity()).getLocation();
                }
            });
        }


        return rootView;
    }

    private void menuSetBold(TextView textView){
        textView.setTypeface(null, Typeface.BOLD);
    }

    private void menuSetNormal(View rootView){
        TextView menu_near_me = (TextView) rootView.findViewById(R.id.menu_near_me);
        TextView menu_trending = (TextView) rootView.findViewById(R.id.menu_trending);
        EditText searchtxt = (EditText) rootView.findViewById(R.id.searchtxt);
        menu_trending.setTypeface(null, Typeface.NORMAL);
        menu_near_me.setTypeface(null, Typeface.NORMAL);
        searchtxt.setTypeface(null, Typeface.NORMAL);
    }

    private void searchSetBold(EditText editText){
        editText.setTypeface(null, Typeface.BOLD);
    }
}