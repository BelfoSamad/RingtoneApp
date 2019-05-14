package com.devalutix.ringtoneapp.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.pojo.Ringtone;
import com.devalutix.ringtoneapp.ui.activities.MainActivity;

import java.util.ArrayList;

public class RingtonesAdapter extends RecyclerView.Adapter<RingtonesAdapter.ViewHolder> {
    private static final String TAG = "ImagesAdapter";

    //Declarations
    private ArrayList<Ringtone> ringtones;
    private MainActivity mView = null;

    public RingtonesAdapter(ArrayList<Ringtone> ringtones, MainActivity mView) {
        this.ringtones = ringtones;
        this.mView = mView;
    }

    @NonNull
    @Override
    public RingtonesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: CreatingViews.");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ringtones_recyclerview_item, parent, false);

        return new RingtonesAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RingtonesAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Setting Views.");

        //Set Title
        holder.title.setText(ringtones.get(position).getRingtoneTitle());
        Glide.with(mView)
                .load(ringtones.get(position).getRingtoneThumbnailUrl())
                .fitCenter()
                //.placeholder(R.drawable.loading_spinner)
                .into(holder.thumbnail);

        holder.drop_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mView, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.actions, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.share_ringtone_action:
                                Toast.makeText(mView, "Sharing...", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.download_ringtone_action:
                                Toast.makeText(mView, "Downloading...", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.set_as_ringtone_action:
                                Toast.makeText(mView, "Setting...", Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        if (ringtones == null) return 0;
        else return ringtones.size();
    }

    /**
     * Clear All the ArrayList
     */
    public void clearAll() {
        if (ringtones != null) ringtones.clear();
        notifyDataSetChanged();
    }

    /**
     * Add the New ArrayList
     *
     * @param newRingtones : the ArrayList to Add
     */
    public void addAll(ArrayList<Ringtone> newRingtones) {
        ringtones = newRingtones;
        notifyDataSetChanged();
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView title;
        ImageButton drop_menu;

        ViewHolder(View v) {
            super(v);

            thumbnail = (ImageView) v.findViewById(R.id.ringtone_thumbnail);
            title = (TextView) v.findViewById(R.id.ringtone_title);
            drop_menu = (ImageButton) v.findViewById(R.id.drop_menu);
        }
    }
}
