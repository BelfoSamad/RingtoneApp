package com.devalutix.ringtoneapp.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.pojo.Ringtone;
import com.devalutix.ringtoneapp.ui.activities.MainActivity;
import com.devalutix.ringtoneapp.ui.activities.RingtonesActivity;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;

import java.util.ArrayList;

public class RingtonesAdapter extends RecyclerView.Adapter<RingtonesAdapter.ViewHolder> {
    private static final String TAG = "ImagesAdapter";

    //Declarations
    private ArrayList<Ringtone> ringtones;
    private MainActivity mView = null;
    private RingtonesActivity mView1 = null;
    private Context mContext;
    private String mode;
    private MediaPlayer mediaPlayer;
    private boolean setting = false;

    //View Declarations
    private ProgressBar loading_curr = null;
    private TextView title_curr = null;
    private ImageView icon_curr = null;
    private int pos_curr = -1;


    public RingtonesAdapter(ArrayList<Ringtone> ringtones, MainActivity mView, String mode) {
        this.ringtones = ringtones;
        this.mView = mView;
        this.mode = mode;
        this.mContext = mView;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public RingtonesAdapter(ArrayList<Ringtone> ringtones, RingtonesActivity mView, String mode) {
        this.ringtones = ringtones;
        this.mView1 = mView;
        this.mode = mode;
        this.mContext = mView;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
        Glide.with(mContext)
                .load(ringtones.get(position).getRingtoneThumbnailUrl())
                .fitCenter()
                //.placeholder(R.drawable.loading_spinner)
                .into(holder.thumbnail);


        Sprite wave = new Wave();
        holder.loading.setIndeterminateDrawable(wave);

        //Listener
        holder.thumbnail.setOnClickListener(v -> {
            if (!setting)
                if (pos_curr == position)
                    stopCurrentSound();
                else if (pos_curr != -1) {
                    stopCurrentSound();
                    setCurrent(position, holder.loading, holder.title, holder.pause_stop);
                    new Player().execute(ringtones.get(position).getRingtoneUrl());
                } else {
                    setCurrent(position, holder.loading, holder.title, holder.pause_stop);
                    new Player().execute(ringtones.get(position).getRingtoneUrl());
                }
        });
        if (mView != null)
            holder.drop_menu.setOnClickListener(v -> mView.halfExpandActionsCard(position, mode));
        else holder.drop_menu.setOnClickListener(v -> mView1.halfExpandActionsCard(position, mode));
    }

    private void setCurrent(int position, ProgressBar loading, TextView title, ImageView icon) {
        Log.d(TAG, "setCurrent: Setting Current - " + pos_curr);
        title_curr = title;
        loading_curr = loading;
        icon_curr = icon;
        pos_curr = position;
    }

    private void resetCurrent() {
        Log.d(TAG, "resetCurrent: Resetting Current - " + pos_curr);
        title_curr = null;
        icon_curr = null;
        loading_curr = null;
        pos_curr = -1;
    }

    private void stopCurrentSound() {
        Log.d(TAG, "stopCurrentSound: Stopping Current Sound - " + pos_curr);
        mediaPlayer.stop();
        mediaPlayer.reset();
        icon_curr.setImageResource(R.drawable.play);
        title_curr.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
        resetCurrent();
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
        ImageView pause_stop;
        ProgressBar loading;

        ViewHolder(View v) {
            super(v);

            thumbnail = v.findViewById(R.id.ringtone_thumbnail);
            title = v.findViewById(R.id.ringtone_title);
            drop_menu = v.findViewById(R.id.drop_menu);
            pause_stop = v.findViewById(R.id.play_stop);
            loading = v.findViewById(R.id.load);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class Player extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            boolean prepared;
            setting = true;
            try {
                mediaPlayer.setDataSource(strings[0]);

                mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                    stopCurrentSound();
                });

                mediaPlayer.prepare();
                prepared = true;

            } catch (Exception e) {
                Log.e("MyAudioStreamingApp", e.getMessage());
                prepared = false;
            }

            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            Log.d(TAG, "onPostExecute: Starting Sound - " + pos_curr);
            icon_curr.setImageResource(R.drawable.stop);
            title_curr.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            mediaPlayer.start();
            loading_curr.setVisibility(View.GONE);
            icon_curr.setVisibility(View.VISIBLE);
            setting = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loading_curr.setVisibility(View.VISIBLE);
            icon_curr.setVisibility(View.GONE);
        }
    }
}
