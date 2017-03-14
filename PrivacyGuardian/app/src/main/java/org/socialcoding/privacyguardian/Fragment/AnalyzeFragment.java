package org.socialcoding.privacyguardian.Fragment;

import android.content.Context;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.socialcoding.privacyguardian.Inteface.MainActivityInterfaces.OnAnalyzeInteractionListener;
import org.socialcoding.privacyguardian.R;
import org.socialcoding.privacyguardian.Structs.ResultItem;
import org.socialcoding.privacyguardian.ResultAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAnalyzeInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AnalyzeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnalyzeFragment extends Fragment {
    ListView listView = null;
    ArrayList<ResultItem> LIST_MENU = new ArrayList<ResultItem>();
    ArrayAdapter arrayAdapter = null;

    private OnAnalyzeInteractionListener mListener;

    public AnalyzeFragment() {
        // Required empty public constructor
    }

    public static AnalyzeFragment newInstance() {
        AnalyzeFragment fragment = new AnalyzeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analyze, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        /* sample1 */
        Button button2 = (Button) view.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSamplePayloadPressed(0);
            }
        });

        /* sample2 */
        Button button3 = (Button) view.findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSamplePayloadPressed(1);
            }
        });

        Button button4 = (Button) view.findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onClearDBButtonPressed();
            }
        });

        Button buttonStartAnalyze = (Button) view.findViewById(R.id.start_analyze);
        buttonStartAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAnalyzeButtonPressed();
            }
        });


        Button buttonGoogleMaps = (Button) view.findViewById(R.id.maps);
        buttonGoogleMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    onMapsButtonPressed();
                }
        });


        arrayAdapter = new ResultAdapter(getContext(), R.layout.result_list_item, LIST_MENU);
        listView = (ListView) view.findViewById(R.id.listview1);
        listView.setAdapter(arrayAdapter);
        refreshList();
    }

    /* interaction with main activity */

    private void onClearDBButtonPressed() {
        if(mListener != null){
            mListener.onClearDBPressed();
            refreshList();
        }
    }

    public void onAnalyzeButtonPressed() {
        if (mListener != null) {
            mListener.onAnalyzePressed();
            refreshList();
        }
    }
    public void onMapsButtonPressed(){
        if(mListener !=null){
            mListener.onMapsPressed();
        }
    }
    public void onSamplePayloadPressed(int i){
        if (mListener!= null){
            mListener.onSamplePayloadPressed(i);
            refreshList();
        }
    }

    public void refreshList() {
        ResultItem[] items = mListener.onListRequired();
        LIST_MENU.clear();
        if(items == null){
            Log.d("refreshList", "no items found");
        }
        else{
            for(ResultItem item : items){
                LIST_MENU.add(item);
            }
        }
        arrayAdapter.notifyDataSetChanged();
        Log.d("refreshList", "list refreshed");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAnalyzeInteractionListener) {
            mListener = (OnAnalyzeInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AnalyzeInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
