package org.socialcoding.privacyguardian.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.socialcoding.privacyguardian.R;
import org.socialcoding.privacyguardian.Inteface.DataSelectActivityInterFaces.OnTypeSelectionChangedListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnTypeSelectionChangedListener} interface
 * to handle interaction events.
 * Use the {@link DataSelectTypeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataSelectTypeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnTypeSelectionChangedListener mListener;

    public DataSelectTypeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DataSelectTypeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataSelectTypeFragment newInstance(String param1, String param2) {
        DataSelectTypeFragment fragment = new DataSelectTypeFragment();
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
        return inflater.inflate(R.layout.fragment_data_select_type, container, false);
    }

    public void onContactButtonPressed() {
        if (mListener != null) {
            mListener.onTypeSelectionChanged("contact");
        }
    }

    public void onLocationButtonPressed() {
        if (mListener != null) {
            mListener.onTypeSelectionChanged("location");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTypeSelectionChangedListener) {
            mListener = (OnTypeSelectionChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDateSelectionChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ImageView buttonContact = (ImageView) view.findViewById(R.id.image_view_type_contact);
        buttonContact.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onContactButtonPressed();
            }
        });

        ImageView buttonLocation = (ImageView) view.findViewById(R.id.image_view_type_location);
        buttonLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onLocationButtonPressed();
            }
        });
    }

}
