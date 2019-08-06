package com.devalutix.ringtoneapp.presenters;

import android.content.ContentValues;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.contracts.RingtonesContract;
import com.devalutix.ringtoneapp.pojo.Ringtone;
import com.devalutix.ringtoneapp.ui.activities.RingtonesActivity;
import com.devalutix.ringtoneapp.utils.ApiEndpointInterface;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import retrofit2.Call;

import static android.app.Activity.RESULT_OK;

public class RingtonesPresenter implements RingtonesContract.Presenter {
    private static final String TAG = "RingtonesPresenter";

    /***************************************** Declarations ***************************************/
    private RingtonesActivity mView;
    private ApiEndpointInterface apiService;
    private ArrayList<Ringtone> ringtones;
    private String mode;
    private Ringtone ringtone;

    /***************************************** Constructor ****************************************/
    public RingtonesPresenter(ApiEndpointInterface apiService) {
        this.apiService = apiService;
    }

    /***************************************** Essential Methods **********************************/
    @Override
    public void attach(RingtonesContract.View view) {
        mView = (RingtonesActivity) view;
    }

    @Override
    public void dettach() {
        mView = null;
    }

    @Override
    public boolean isAttached() {
        return !(mView == null);
    }

    /***************************************** Methods ********************************************/
    @Override
    public void initRecyclerView(String name) {

        Call<ArrayList<Ringtone>> call = null;
        ringtones = getRingtones();

        //        switch (mode) {
//            case "category": {
//                call = apiService.getCategoryRingtones(Config.TOKEN, name);
//            }
//            break;
//            case "ringtones": {
//                call = apiService.getRingtones(Config.TOKEN, name);
//            }
//            break;
//            case "search": {
//                call = apiService.searchRingtones(Config.TOKEN, name);
//            }
//            break;
//        }
//
//        call.enqueue(new Callback<ArrayList<Ringtone>>() {
//            @Override
//            public void onResponse(@NonNull Call<ArrayList<Ringtone>> call,
//                                   @NonNull Response<ArrayList<Ringtone>> response) {
//                if (response.isSuccessful()) {
//                    ringtones = response.body();
//                    mView.initRecyclerView(ringtones);
//                    mView.showRingtones();
//
//                    assert response.body() != null;
//                    if (response.body().size() > 0) mView.showRingtones();
//                    else {
//                        if (mode.equals("search"))
//                            mView.showEmptyResults(mView.getResources().getString(R.string.empty_search));
//                        else mView.showEmptyResults(mView.getResources().getString(R.string.empty_results));
//                    }
//                } else {
//                    mView.initRecyclerView(null);
//                    mView.showNoRingtone();
//                    (mView).showRetryCard(mView.getResources().getString(R.string.server_prblm_retry));
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ArrayList<Ringtone>> call, @NonNull Throwable t) {
//                mView.initRecyclerView(null);
//                mView.showNoRingtone();
//                mView.showRetryCard(mView.getResources().getString(R.string.net_prblm_retry));
//            }
//        });

        mView.initRecyclerView(ringtones);

        if (ringtones == null) mView.showNoRingtone();
        else if (ringtones.size() > 0) mView.showRingtones();
        else mView.showEmptyResults("No Content");
    }

    @Override
    public void updateRecyclerView(String name) {

        Call<ArrayList<Ringtone>> call = null;
        ringtones = getRingtones();

        //        switch (mode) {
//            case "category": {
//                call = apiService.getCategoryRingtones(Config.TOKEN, name);
//            }
//            break;
//            case "ringtones": {
//                call = apiService.getRingtones(Config.TOKEN, name);
//            }
//            break;
//            case "search": {
//                call = apiService.searchRingtones(Config.TOKEN, name);
//            }
//            break;
//        }
//
//        call.enqueue(new Callback<ArrayList<Ringtone>>() {
//            @Override
//            public void onResponse(@NonNull Call<ArrayList<Ringtone>> call,
//                                   @NonNull Response<ArrayList<Ringtone>> response) {
//                if (response.isSuccessful()) {
//                    ringtones = response.body();
//                    mView.updateRecyclerView(ringtones);
//                    mView.showRingtones();
//
//                    assert response.body() != null;
//                    if (response.body().size() > 0) mView.showRingtones();
//                    else {
//                        if (mode.equals("search"))
//                            mView.showEmptyResults(mView.getResources().getString(R.string.empty_search));
//                        else mView.showEmptyResults(mView.getResources().getString(R.string.empty_results));
//                    }
//                } else {
//                    mView.updateRecyclerView(null);
//                    mView.showNoRingtone();
//                    (mView).showRetryCard(mView.getResources().getString(R.string.server_prblm_retry));
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ArrayList<Ringtone>> call, @NonNull Throwable t) {
//                mView.updateRecyclerView(null);
//                mView.showNoRingtone();
//                mView.showRetryCard(mView.getResources().getString(R.string.net_prblm_retry));
//            }
//        });

        mView.updateRecyclerView(ringtones);

        if (ringtones == null) mView.showNoRingtone();
        else if (ringtones.size() > 0) mView.showRingtones();
        else mView.showEmptyResults("No Content");
    }

    private ArrayList<Ringtone> getRingtones() {
        String json = null;
        try {
            InputStream is = mView.getAssets().open("ringtones.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        Ringtone[] collectionItem = new Gson().fromJson(json, Ringtone[].class);
        return new ArrayList<Ringtone>(Arrays.asList(collectionItem));
    }

    @Override
    public void saveRingtone(String type) {
        Log.d(TAG, "saveRingtone: Saving Ringtone");

        String downloadAudioPath = getPath();

        if (fileExists(downloadAudioPath))
            Toast.makeText(mView, "Ringtone Already Downloaded", Toast.LENGTH_SHORT).show();
        else
            new RingtonesPresenter.DownloadFile().execute(ringtone.getRingtoneUrl(), downloadAudioPath, type);
    }

    private String getPath() {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).getPath();
        File myDir = new File(root);
        myDir.mkdirs();
        return root + File.separator + getFileName();
    }

    private String getFileName() {
        String url = ringtone.getRingtoneUrl();
        String name = ringtone.getRingtoneTitle();
        if (url.endsWith(".mp3"))
            name += ".mp3";
        else if (url.endsWith(".wav"))
            name += ".wav";
        else if (url.endsWith(".m4u"))
            name += ".m4u";

        return name;
    }

    private File getFile(String uri) {
        return new File(uri);
    }

    private boolean fileExists(String uri) {
        return getFile(uri).exists();
    }

    @Override
    public void shareRingtone() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, mView.getResources().getString(R.string.app_name));
        String sAux = mView.getResources().getString(R.string.share_msg_1) + " " + ringtone.getRingtoneTitle()
                + " " + mView.getResources().getString(R.string.share_msg_2)
                + "\n" + "https://play.google.com/store/apps/details?id=" + mView.getPackageName();
        i.putExtra(Intent.EXTRA_TEXT, sAux);
        mView.startActivity(Intent.createChooser(i, "choose one"));
    }

    @Override
    public void setAsRingtone() {
        String path = getPath();
        File file;
        if (fileExists(path)) {
            file = getFile(path);
            // Create the database record, pointing to the existing file path
            String mimeType = "";
            if (path.endsWith(".m4a")) {
                mimeType = "audio/mp4a-latm";
            } else if (path.endsWith(".wav")) {
                mimeType = "audio/wav";
            } else if (path.endsWith(".mp3")) {
                mimeType = "audio/mp3";
            }

            String artist = "" + mView.getResources().getText(R.string.app_name);

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            values.put(MediaStore.MediaColumns.TITLE, ringtone.getRingtoneTitle());
            values.put(MediaStore.MediaColumns.SIZE, file.length());
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

            values.put(MediaStore.Audio.Media.ARTIST, artist);
            //values.put(MediaStore.Audio.Media.DURATION, duration);

            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            // Insert it into the database
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
            final Uri newUri = mView.getContentResolver().insert(uri, values);
            mView.setResult(RESULT_OK, new Intent().setData(newUri));

            RingtoneManager.setActualDefaultRingtoneUri(
                    mView,
                    RingtoneManager.TYPE_RINGTONE,
                    newUri);
        } else {
            saveRingtone("ringtone");
        }
    }

    @Override
    public void setAsNotification() {
        String path = getPath();
        File file;
        if (fileExists(path)) {
            file = getFile(path);
            // Create the database record, pointing to the existing file path
            String mimeType = "";
            if (path.endsWith(".m4a")) {
                mimeType = "audio/mp4a-latm";
            } else if (path.endsWith(".wav")) {
                mimeType = "audio/wav";
            } else if (path.endsWith(".mp3")) {
                mimeType = "audio/mp3";
            }

            String artist = "" + mView.getResources().getText(R.string.app_name);

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            values.put(MediaStore.MediaColumns.TITLE, ringtone.getRingtoneTitle());
            values.put(MediaStore.MediaColumns.SIZE, file.length());
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

            values.put(MediaStore.Audio.Media.ARTIST, artist);
            //values.put(MediaStore.Audio.Media.DURATION, duration);

            values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            // Insert it into the database
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
            final Uri newUri = mView.getContentResolver().insert(uri, values);
            mView.setResult(RESULT_OK, new Intent().setData(newUri));

            RingtoneManager.setActualDefaultRingtoneUri(
                    mView,
                    RingtoneManager.TYPE_NOTIFICATION,
                    newUri);
        } else {
            saveRingtone("notification");
        }
    }

    @Override
    public void setAsAlarm() {
        String path = getPath();
        File file;
        if (fileExists(path)) {
            file = getFile(path);
            // Create the database record, pointing to the existing file path
            String mimeType = "";
            if (path.endsWith(".m4a")) {
                mimeType = "audio/mp4a-latm";
            } else if (path.endsWith(".wav")) {
                mimeType = "audio/wav";
            } else if (path.endsWith(".mp3")) {
                mimeType = "audio/mp3";
            }

            String artist = "" + mView.getResources().getText(R.string.app_name);

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            values.put(MediaStore.MediaColumns.TITLE, ringtone.getRingtoneTitle());
            values.put(MediaStore.MediaColumns.SIZE, file.length());
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

            values.put(MediaStore.Audio.Media.ARTIST, artist);
            //values.put(MediaStore.Audio.Media.DURATION, duration);

            values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_ALARM, true);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            // Insert it into the database
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(path);
            final Uri newUri = mView.getContentResolver().insert(uri, values);
            mView.setResult(RESULT_OK, new Intent().setData(newUri));
        } else {
            saveRingtone("alarm");
        }
    }

    @Override
    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public void setRingtoneObject(int position) {
        ringtone = ringtones.get(position);
    }

    private class DownloadFile extends AsyncTask<String, Integer, String> {
        int count;

        @Override
        protected String doInBackground(String... url) {
            try {
                Log.d(TAG, "doInBackground: download url = " + url[0]);
                URL urls = new URL(url[0]);
                URLConnection connection = urls.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(urls.openStream());
                Log.d(TAG, "doInBackground: Where To Put It = " + url[1]);
                OutputStream output = new FileOutputStream(url[1]);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    publishProgress((int) (total * 100 / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                switch (url[2]) {
                    case "ringtone":
                        setAsRingtone();
                        break;
                    case "notification":
                        setAsNotification();
                        break;
                    case "alarm":
                        setAsAlarm();
                        break;
                }
            } catch (Exception e) {
                Log.d(TAG, "doInBackground: Donwload Problem");
            }
            return null;
        }
    }
}
