package layout;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.disxc.anonymous.Analyzer;
import com.example.disxc.anonymous.CacheMaker;
import com.example.disxc.anonymous.DatabaseHelper;
import com.example.disxc.anonymous.R;
import com.example.disxc.anonymous.DatabaseHelper.LogEntry;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAnalyzeInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Analyze#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Analyze extends Fragment
        implements Analyzer.onLogGeneratedListener, Button.OnClickListener {
    CacheMaker cm = null;
    Analyzer analyzer = null;
    ListView listView = null;
    ArrayList<String> LIST_MENU = new ArrayList<String>();
    ArrayAdapter arrayAdapter = null;
    DatabaseHelper mDatabase;

    private OnAnalyzeInteractionListener mListener;

    public Analyze() {
        // Required empty public constructor
    }

    public static Analyze newInstance() {
        Analyze fragment = new Analyze();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_analyze, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        /* button perform update */
        Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);

        /* sample1 */
        Button button2 = (Button) view.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (analyzer != null)
                    analyzer.runSamplePayload(0);
            }
        });

        /* sample2 */
        Button button3 = (Button) view.findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (analyzer != null)
                    analyzer.runSamplePayload(1);
            }
        });

        Button button4 = (Button) view.findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mDatabase.clearDB();
                onLogGenerated("NULL");
            }
        });

        arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, LIST_MENU);
        listView = (ListView) view.findViewById(R.id.listview1);
        listView.setAdapter(arrayAdapter);
        onLogGenerated("LOG");
        //super.onViewCreated(view, savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onAnalyzeInteraction();
        }
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

    @Override
    public void onLogGenerated(String log) {
        SQLiteDatabase db = mDatabase.getReadableDatabase();
        //TODO: 내 언어에 맞는 시간대 출력하는 방법 찾기

        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("mmm dd HH:mm");

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                LogEntry._ID,
                LogEntry.COLUMN_DATETIME,
                LogEntry.COLUMN_PACKAGE_NAME,
                LogEntry.COLUMN_DATA_TYPE,
                LogEntry.COLUMN_DATA_VALUE
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = LogEntry.COLUMN_PACKAGE_NAME + " = ?";
        String[] selectionArgs = {"*"};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                LogEntry.COLUMN_DATETIME + " DESC";

        Cursor c = db.query(
                LogEntry.TABLE_NAME,                      // The table to query
                projection,                               // The columns to return
                null,                                     // The not columns for the WHERE clause
                null,                                     // The not values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        Log.d("onLogGenerated", "query got " + c.getCount());
        LIST_MENU.clear();
        if(c.moveToFirst()){
            do{
                Date date = null;
                String str = "";
                str += c.getString(0);/*
                //TODO: 제대로 된 LOCALE 시간 알아내기
                try{
                    date = inputFormat.parse(c.getString(1));
                    str += outputFormat.format(date);
                }
                catch(Exception e){
                    e.printStackTrace();
                }*/
                str += ", " + c.getString(2);
                str += ", " + c.getString(3);
                str += ", " + c.getString(4);
                LIST_MENU.add(str);
            }while(c.moveToNext());
        }
        arrayAdapter.notifyDataSetChanged();
    }

    //button when update button pressed
    @Override
    public void onClick(View v) {
        try {
            cm = new CacheMaker(getContext());
            analyzer = new Analyzer(cm, getContext());
            analyzer.setOnLogGenerated(this);

        } catch(Exception e){
            e.printStackTrace();
            Log.d("button", "something Wrong...");
        }
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
        void onAnalyzeInteraction();
    }

}
