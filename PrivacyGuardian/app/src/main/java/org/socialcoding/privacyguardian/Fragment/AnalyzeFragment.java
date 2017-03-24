package org.socialcoding.privacyguardian.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import org.socialcoding.privacyguardian.Structs.SensitiveInfoTypes;

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
        Button addDBButton = (Button) view.findViewById(R.id.button_add_to_db);
        addDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDBDialog();
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
                    onMapsButtonPressed(LIST_MENU);
                }
        });


        arrayAdapter = new ResultAdapter(getContext(), R.layout.result_list_item, LIST_MENU);
        listView = (ListView) view.findViewById(R.id.listview1);
        listView.setAdapter(arrayAdapter);
        refreshList();
    }

    private void showDBDialog() {
        AddDatabaseDialogFragment dialog = new AddDatabaseDialogFragment();
        dialog.show(getFragmentManager(), "mytag");
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
    public void onMapsButtonPressed(ArrayList<ResultItem> LIST_MENU){
        if(mListener !=null){
            mListener.onMapsPressed(LIST_MENU);
        }
    }

    public void refreshList() {
        if(mListener == null){
            return;
        }
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
