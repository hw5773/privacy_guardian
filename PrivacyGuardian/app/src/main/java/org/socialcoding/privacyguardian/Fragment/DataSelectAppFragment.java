package org.socialcoding.privacyguardian.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.socialcoding.privacyguardian.Activity.DataSelectActivity;
import org.socialcoding.privacyguardian.AppsItemRecyclerViewAdapter;
import org.socialcoding.privacyguardian.Inteface.DataSelectActivityInterFaces;
import org.socialcoding.privacyguardian.R;
import org.socialcoding.privacyguardian.Fragment.AppsItem.DataSelectAppContent;
import org.socialcoding.privacyguardian.Inteface.DataSelectActivityInterFaces.OnAppSelectionInteractionListener;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnAppSelectionInteractionListener}
 * interface.
 */
public class DataSelectAppFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnAppSelectionInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DataSelectAppFragment() {
    }

    // TODO: Customize parameter initialization
    public static DataSelectAppFragment newInstance(int columnCount) {
        DataSelectAppFragment fragment = new DataSelectAppFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataSelectAppContent.init(DataSelectActivity.appsList, mListener.onAppCacheDemanded());
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_select_app, container, false);
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.ds_applist);
        // Set the adapter
        if (recyclerView != null) {
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new AppsItemRecyclerViewAdapter(DataSelectAppContent.ITEMS, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DataSelectActivityInterFaces.OnAppSelectionInteractionListener) {
            mListener = (DataSelectActivityInterFaces.OnAppSelectionInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                + " must implement OnAppSelectionInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
