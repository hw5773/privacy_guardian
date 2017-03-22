package org.socialcoding.privacyguardian.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.socialcoding.privacyguardian.Inteface.MainActivityInterfaces.DatabaseDialogListener;
import org.socialcoding.privacyguardian.R;
import org.socialcoding.privacyguardian.Structs.SensitiveInfoTypes;

import java.util.Calendar;

/**
 * Created by disxc on 2017-03-21.
 */

public class AddDatabaseDialogFragment extends DialogFragment {
    DatabaseDialogListener mListener;

    EditText mPackage, mTime, mIp, mType, mValue;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_adddb, null);

        mPackage = (EditText) view.findViewById(R.id.add_packagename);
        mTime = (EditText) view.findViewById(R.id.add_time);
        mIp = (EditText) view.findViewById(R.id.add_ip);
        mType = (EditText) view.findViewById(R.id.add_type);
        mValue = (EditText) view.findViewById(R.id.add_value);


        mPackage.setText("com.example.app");
        mTime.setText(Long.valueOf(Calendar.getInstance().getTime().getTime()).toString());
        mIp.setText("127.0.0.1");
        mType.setText(SensitiveInfoTypes.TYPE_LOCATION_LATLNG);
        mValue.setText("12.0;34.0");

        builder.setView(view)
                .setPositiveButton(R.string.add_sample_db, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(mPackage.getText().toString(),
                                Long.parseLong(mTime.getText().toString()),
                                mIp.getText().toString(),
                                mType.getText().toString(),
                                mValue.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });


        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DatabaseDialogListener) getActivity();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }

    }
}
