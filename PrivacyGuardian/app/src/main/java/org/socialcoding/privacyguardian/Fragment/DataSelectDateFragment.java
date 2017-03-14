package org.socialcoding.privacyguardian.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.socialcoding.privacyguardian.R;

import java.util.Calendar;

import org.socialcoding.privacyguardian.Inteface.DataSelectActivityInterFaces.OnDateSelectionChangedListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnDateSelectionChangedListener} interface
 * to handle interaction events.
 * Use the {@link DataSelectDateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataSelectDateFragment extends Fragment {

    private static final String START_DATE = "param1";
    private static final String END_DATE = "param2";
    private static final int YEAR = Calendar.YEAR;
    private static final int MONTH = Calendar.MONTH;
    private static final int DATE = Calendar.DATE;

    private Calendar startCal;
    private Calendar endCal;

    private OnDateSelectionChangedListener mListener;

    public DataSelectDateFragment() {
        // Required empty public constructor
    }

    /**
     * @param startDate Parameter 1.
     * @param EndDate Parameter 2.
     * @return A new instance of fragment DataSelectDateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataSelectDateFragment newInstance(Long startDate, Long EndDate) {
        DataSelectDateFragment fragment = new DataSelectDateFragment();
        Bundle args = new Bundle();
        args.putLong(START_DATE, startDate);
        args.putLong(END_DATE, EndDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            startCal = Calendar.getInstance();
            startCal.setTimeInMillis(getArguments().getLong(START_DATE));
            endCal = Calendar.getInstance();
            endCal.setTimeInMillis(getArguments().getLong(END_DATE));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_data_select_date, container, false);
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
        datePicker1.init(startCal.get(YEAR), startCal.get(MONTH), startCal.get(DATE), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                mListener.onStartDateSelectionChanged(calendar);
            }
        });
        DatePicker datePicker2 = (DatePicker) view.findViewById(R.id.ds_end_dp);
        datePicker2.init(endCal.get(YEAR), endCal.get(MONTH), endCal.get(DATE), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                mListener.onEndDateSelectionChanged(calendar);
            }
        });
    }

}
