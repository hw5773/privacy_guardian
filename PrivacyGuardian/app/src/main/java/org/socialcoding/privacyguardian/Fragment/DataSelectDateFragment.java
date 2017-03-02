package org.socialcoding.privacyguardian.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.DatePicker;

import org.socialcoding.privacyguardian.R;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnDateSelectionChangedListener} interface
 * to handle interaction events.
 * Use the {@link DataSelectDateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataSelectDateFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnDateSelectionChangedListener mListener;

    public DataSelectDateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DataSelectDateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataSelectDateFragment newInstance(String param1, String param2) {
        DataSelectDateFragment fragment = new DataSelectDateFragment();
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
        return inflater.inflate(R.layout.fragment_data_select_date, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        //TODO: DO SOMETHING!!!
        if (mListener != null) {
            //mListener.onStartDateSelectionChanged(new Date(), new Date());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDateSelectionChangedListener) {
            mListener = (OnDateSelectionChangedListener) context;
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
        DatePicker datePicker1 = (DatePicker) view.findViewById(R.id.ds_start_dp);
        datePicker1.init(1992, 11, 22, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                mListener.onStartDateSelectionChanged(calendar);
            }
        });
        DatePicker datePicker2 = (DatePicker) view.findViewById(R.id.ds_end_dp);
        datePicker2.init(2016, 11, 22, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                mListener.onEndDateSelectionChanged(calendar);
            }
        });
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
    public interface OnDateSelectionChangedListener {
        void onStartDateSelectionChanged(Calendar start);
        void onEndDateSelectionChanged(Calendar end);
    }
}
