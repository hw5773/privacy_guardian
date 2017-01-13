package layout;

import android.content.Context;
import android.net.Uri;
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
import android.widget.Toast;

import com.example.disxc.anonymous.Analyzer;
import com.example.disxc.anonymous.CacheMaker;
import com.example.disxc.anonymous.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnAnalyzeInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Analyze#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Analyze extends Fragment {
    //Analyze Arguments
    CacheMaker cm = null;
    Analyzer analyzer = null;
    ListView listView = null;
    ArrayList<String> LIST_MENU = new ArrayList<String>();
    ArrayAdapter arrayAdapter = null;

    private OnAnalyzeInteractionListener mListener;

    public Analyze() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static Analyze newInstance() {
        Analyze fragment = new Analyze();
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
        /* button perform update */
        Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cm = new CacheMaker(getContext());
                    analyzer = new Analyzer(cm, getContext());
                    analyzer.setOnLogGenerated(new Analyzer.onLogGeneratedListener(){
                        @Override
                        public void onLogGenerated(String log) {
                            LIST_MENU.add(LIST_MENU.size() + "." + log);
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });
                } catch(Exception e){
                    Log.d("button", "something Wrong...");
                }
            }
        });

        /* button check if it has updated */
        Button button2 = (Button) view.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() { // this button performs DB update!
            @Override
            public void onClick(View v) {
                if(analyzer != null)
                    analyzer.sample(0);
            }
        });

        /* button Analyze */
        Button button3 = (Button) view.findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(analyzer != null)
                    analyzer.sample(1);
            }
        });

        arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, LIST_MENU);
        listView = (ListView) view.findViewById(R.id.listview1);
        listView.setAdapter(arrayAdapter);
        //super.onViewCreated(view, savedInstanceState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onAnalyzeInteraction();
        }
        //getActivity().runOnUiThread();
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
        void onAnalyzeInteraction();
    }

}
