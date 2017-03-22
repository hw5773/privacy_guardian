package org.socialcoding.privacyguardian;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import org.socialcoding.privacyguardian.Fragment.AppsItem.DataSelectAppContent.AppsItem;
import org.socialcoding.privacyguardian.Inteface.DataSelectActivityInterFaces.OnAppSelectionInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AppsItem} and makes a call to the
 * specified {@link OnAppSelectionInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AppsItemRecyclerViewAdapter extends RecyclerView.Adapter<AppsItemRecyclerViewAdapter.ViewHolder> {

    private final List<AppsItem> mValues;
    private final OnAppSelectionInteractionListener mListener;

    public AppsItemRecyclerViewAdapter(List<AppsItem> items, OnAppSelectionInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mImageView.setImageDrawable(mValues.get(position).image);
        holder.mContentView.setText(mValues.get(position).content);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: DO app list changed action.
                if(!holder.mCheckBox.isChecked()){
                    holder.mCheckBox.setChecked(true);
                }
                else{
                    holder.mCheckBox.setChecked(false);
                }
                if (null != mListener) {
                    mListener.onAppSelectionChanged(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        //public final TextView mIdView;
        public final TextView mContentView;
        public final ImageView mImageView;
        public final CheckBox mCheckBox;
        public AppsItem mItem;
        public boolean isChecked;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.image);
            mContentView = (TextView) view.findViewById(R.id.content);
            mCheckBox = (CheckBox) view.findViewById(R.id.app_checkbox);
            isChecked = false;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

}
