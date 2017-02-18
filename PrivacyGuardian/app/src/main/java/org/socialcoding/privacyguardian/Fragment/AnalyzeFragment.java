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

import org.socialcoding.privacyguardian.R;

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
    ArrayList<String> LIST_MENU = new ArrayList<String>();
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

        arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, LIST_MENU);
        listView = (ListView) view.findViewById(R.id.listview1);
        listView.setAdapter(arrayAdapter);
        refreshList();
        //super.onViewCreated(view, savedInstanceState);
    }

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

    public void onSamplePayloadPressed(int i){
        if (mListener!= null){
            mListener.onSamplePayloadPressed(i);
            refreshList();
        }
    }

    public void refreshList() {
        String[] items = mListener.onListRequired();
        LIST_MENU.clear();
        if(items == null){
            Log.d("refreshList", "no items found");
        }
        else{
            for(String str : items){
                LIST_MENU.add(str);
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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAnalyzeInteractionListener {
        void onAnalyzePressed();
        void onSamplePayloadPressed(int index);
        void onClearDBPressed();
        String[] onListRequired();
    }

}
