package org.socialcoding.privacyguardian;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        //holder.mIdView.setText(mValues.get(position).appName);
        holder.mImageView.setImageResource(R.drawable.poro);
        holder.mContentView.setText(mValues.get(position).content);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), holder.mItem.content, Toast.LENGTH_SHORT).show();
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
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
        public AppsItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.image);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

}
