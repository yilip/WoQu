package com.lip.woqu.activity.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.lip.woqu.R;
import com.lip.woqu.activity.ActivityListActivity;
import com.lip.woqu.utils.UtilsManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link HomeActivityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeActivityFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //图片的长和宽
    private int width;
    private int height;
    private RelativeLayout hotRL,mineRL,sportRL,entertainmentRL,entertainmentRL2,relaxtionRL,studyRL,otherRL;
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeActivityFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeActivityFragment newInstance(String param1, String param2) {
        HomeActivityFragment fragment = new HomeActivityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        DisplayMetrics dm = new DisplayMetrics();
        dm = getActivity().getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        width = (screenWidth - UtilsManager.dip2px(getActivity(), 24)) / 2;
        height = width * 3 / 4;
    }
    private void initViews(View view)
    {
        hotRL=(RelativeLayout)view.findViewById(R.id.topic_hot_rl);
        mineRL=(RelativeLayout)view.findViewById(R.id.topic_mine_rl);
        sportRL=(RelativeLayout)view.findViewById(R.id.topic_sport_rl);
        entertainmentRL=(RelativeLayout)view.findViewById(R.id.topic_enterainment_rl);
        relaxtionRL=(RelativeLayout)view.findViewById(R.id.topic_relaxation_rl);
        studyRL=(RelativeLayout)view.findViewById(R.id.topic_study_rl);
        otherRL=(RelativeLayout)view.findViewById(R.id.topic_other_rl);
        entertainmentRL2=(RelativeLayout)view.findViewById(R.id.topic_enterainment_rl2);
        //ViewGroup.LayoutParams lp0=hotRL.getLayoutParams();
        RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)hotRL.getLayoutParams();
        params.width=width;
        params.height=height-20;
        hotRL.setLayoutParams(params);

        params=(RelativeLayout.LayoutParams)mineRL.getLayoutParams();
        params.width=width;
        params.height=height-20;
        mineRL.setLayoutParams(params);

        params=(RelativeLayout.LayoutParams)sportRL.getLayoutParams();
        params.width=width;
        params.height=height*2+16;
        sportRL.setLayoutParams(params);
        params=(RelativeLayout.LayoutParams)entertainmentRL2.getLayoutParams();
        params.width=width;
        params.height=height*2+16;
        entertainmentRL2.setLayoutParams(params);

        params=(RelativeLayout.LayoutParams)entertainmentRL.getLayoutParams();
        params.width=width;
        params.height=height;
        entertainmentRL.setLayoutParams(params);

        params=(RelativeLayout.LayoutParams)relaxtionRL.getLayoutParams();
        params.width=width;
        params.height=height;
        relaxtionRL.setLayoutParams(params);

        params=(RelativeLayout.LayoutParams)studyRL.getLayoutParams();
        params.width=width;
        params.height=height;
        studyRL.setLayoutParams(params);
        params=(RelativeLayout.LayoutParams)otherRL.getLayoutParams();
        params.width=width;
        params.height=height;
        otherRL.setLayoutParams(params);

        hotRL.setOnClickListener(this);
        mineRL.setOnClickListener(this);
        sportRL.setOnClickListener(this);
        entertainmentRL.setOnClickListener(this);
        relaxtionRL.setOnClickListener(this);
        studyRL.setOnClickListener(this);
        otherRL.setOnClickListener(this);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_home_activity, container, false);
        initViews(view);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent(getActivity(), ActivityListActivity.class);;
       switch (v.getId())
       {
           case R.id.topic_hot_rl:
               intent.putExtra("type",-1);
               break;
           case R.id.topic_mine_rl:
               intent.putExtra("type",-1);
               break;
           case R.id.topic_sport_rl:
               intent.putExtra("type",1);
               break;
           case R.id.topic_enterainment_rl:
               intent.putExtra("type",2);
               break;
           case R.id.topic_relaxation_rl:
               intent.putExtra("type",3);
               break;
           case R.id.topic_study_rl:
               intent.putExtra("type",4);
               break;
           case R.id.topic_other_rl:
               intent.putExtra("type",0);
               break;
           default:
               intent.putExtra("type",-1);
       }
        startActivity(intent);
    }
}
