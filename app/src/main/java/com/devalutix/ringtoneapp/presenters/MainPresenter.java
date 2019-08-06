package com.devalutix.ringtoneapp.presenters;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.contracts.MainContract;
import com.devalutix.ringtoneapp.models.SharedPreferencesHelper;
import com.devalutix.ringtoneapp.pojo.Category;
import com.devalutix.ringtoneapp.pojo.Ringtone;
import com.devalutix.ringtoneapp.ui.activities.MainActivity;
import com.devalutix.ringtoneapp.ui.activities.RingtonesActivity;
import com.devalutix.ringtoneapp.utils.ApiEndpointInterface;
import com.devalutix.ringtoneapp.utils.GDPR;
import com.devalutix.ringtoneapp.utils.PermissionUtil;
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

import static android.app.Activity.RESULT_OK;

public class MainPresenter implements MainContract.Presenter {
    private static String TAG = "MainPresenter";

    //Declarations
    private MainActivity mView;
    private PermissionUtil mPermissionUtil;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private GDPR gdpr;
    private ApiEndpointInterface apiService;
    private ArrayList<Ringtone> popularRingtones;
    private ArrayList<Ringtone> recentRingtones;
    private Ringtone ringtone;

    //Constructor
    public MainPresenter(PermissionUtil mPermissionUtil, SharedPreferencesHelper sharedPreferencesHelper,
                         GDPR gdpr, ApiEndpointInterface apiService) {
        this.mPermissionUtil = mPermissionUtil;
        this.sharedPreferencesHelper = sharedPreferencesHelper;
        this.gdpr = gdpr;
        this.apiService = apiService;
    }

    //Essential Methods
    @Override
    public void attach(MainContract.View view) {
        mView = (MainActivity) view;
    }

    @Override
    public void dettach() {
        mView = null;
    }

    @Override
    public boolean isAttached() {
        return !(mView == null);
    }

    //Methods
    @Override
    public void requestPermission(String permission, int permissionRequest) {
        mPermissionUtil.checkPermission(mView, permission,
                new PermissionUtil.PermissionAskListener() {
                    @Override
                    public void onNeedPermission() {
                        Log.d(TAG, "onNeedPermission: Demand Permission");
                        ActivityCompat.requestPermissions(
                                mView,
                                new String[]{permission},
                                permissionRequest
                        );
                        if (!ActivityCompat.shouldShowRequestPermissionRationale
                                (mView, permission))
                            onPermissionGranted();
                    }

                    @Override
                    public void onPermissionPreviouslyDenied() {
                        Log.d(TAG, "onPermissionPreviouslyDenied: Permission denied");
                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            final int BUTTON_NEGATIVE = -2;
                            final int BUTTON_POSITIVE = -1;

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case BUTTON_NEGATIVE:
                                        //which = -2
                                        dialog.dismiss();
                                        break;
                                    case BUTTON_POSITIVE:
                                        //which = -1
                                        ActivityCompat.requestPermissions(
                                                mView,
                                                new String[]{permission},
                                                permissionRequest
                                        );
                                        dialog.dismiss();
                                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                                mView, permission))
                                            onPermissionGranted();
                                }
                            }
                        };
                        new AlertDialog.Builder(mView)
                                .setMessage("You need this Permission to download the wallpapers")
                                .setTitle("Needs Permission")
                                .setPositiveButton("Alright", listener)
                                .setNegativeButton("No", listener)
                                .create()
                                .show();
                        //show a dialog explaining permission and then request permission
                    }

                    @Override
                    public void onPermissionDisabled() {
                        Log.d(TAG, "onPermissionDisabled: Permission Denied");
                        //Not Granted
                        sharedPreferencesHelper.setDownloadEnable(false);
                    }

                    @Override
                    public void onPermissionGranted() {
                        Log.d(TAG, "onPermissionGranted: Permission Granted");
                        //Granted
                        sharedPreferencesHelper.setDownloadEnable(true);
                    }
                });
    }

    @Override
    public void initViewPager() {
        ArrayList<Category> categories = getCategories();

        //TODO: Comment Out the Server Call
        //TODO: Remove Dummy Data

//        Call<ArrayList<Category>> call = apiService.getCategories(Config.TOKEN);
//
//        call.enqueue(new Callback<ArrayList<Category>>() {
//            @Override
//            public void onResponse(@NonNull Call<ArrayList<Category>> call,
//                                   @NonNull Response<ArrayList<Category>> response) {
//                if (response.isSuccessful()) {
//                    mView.initViewPager(response.body());
//                    mView.showCategories();
//                } else {
//                    mView.initViewPager(null);
//                    mView.showNoCatgeories();
//                    mView.showRetryCard(mView.getResources().getString(R.string.server_prblm_retry));
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ArrayList<Category>> call, @NonNull Throwable t) {
//                mView.initViewPager(null);
//                mView.showNoCatgeories();
//                mView.showRetryCard(mView.getResources().getString(R.string.net_prblm_retry));
//            }
//        });

        mView.initViewPager(categories);

        if (categories != null) mView.showCategories();
        else mView.showNoCatgeories();
    }

    @Override
    public void initRecyclerView() {
        popularRingtones = getPopular();
        recentRingtones = getRecent();

        //TODO: Comment Out the Server Call
        //TODO: Remove Dummy Data

//        Call<ArrayList<Ringtone>> call = apiService.getRingtones(Config.TOKEN, "recent");
//        call.enqueue(new Callback<ArrayList<Ringtone>>() {
//            @Override
//            public void onResponse(@NonNull Call<ArrayList<Ringtone>> call,
//                                   @NonNull Response<ArrayList<Ringtone>> response) {
//                if (response.isSuccessful()) {
//                    mView.initRecyclerView(response.body(), "recent");
//                    mView.showRingtones();
//                } else {
//                    mView.initRecyclerView(null, "recent");
//                    mView.showNoRingtones();
//                    mView.showRetryCard(mView.getResources().getString(R.string.server_prblm_retry));
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ArrayList<Ringtone>> call, @NonNull Throwable t) {
//                mView.initRecyclerView(null, "recent");
//                mView.showNoRingtones();
//                mView.showRetryCard(mView.getResources().getString(R.string.net_prblm_retry));
//            }
//        });
//
//        Call<ArrayList<Ringtone>> call1 = apiService.getRingtones(Config.TOKEN, "popular");
//        call1.enqueue(new Callback<ArrayList<Ringtone>>() {
//            @Override
//            public void onResponse(@NonNull Call<ArrayList<Ringtone>> call,
//                                   @NonNull Response<ArrayList<Ringtone>> response) {
//                if (response.isSuccessful()) {
//                    mView.initRecyclerView(response.body(), "popular");
//                    mView.showRingtones();
//                } else {
//                    mView.initRecyclerView(null, "popular");
//                    mView.showNoRingtones();
//                    mView.showRetryCard(mView.getResources().getString(R.string.server_prblm_retry));
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ArrayList<Ringtone>> call, @NonNull Throwable t) {
//                mView.initRecyclerView(null, "popular");
//                mView.showNoRingtones();
//                mView.showRetryCard(mView.getResources().getString(R.string.net_prblm_retry));
//            }
//        });

        mView.initRecyclerView(popularRingtones, "popular");
        mView.initRecyclerView(recentRingtones, "recent");

        if (popularRingtones != null && recentRingtones != null) mView.showRingtones();
        else mView.showNoRingtones();
    }

    @Override
    public void updateAll() {
        updateViewPager();
        updateRecyclerView();
    }

    @Override
    public void updateViewPager() {
        ArrayList<Category> categories = getCategories();

        //TODO: Comment Out the Server Call
        //TODO: Remove Dummy Data

//        Call<ArrayList<Category>> call = apiService.getCategories(Config.TOKEN);
//
//        call.enqueue(new Callback<ArrayList<Category>>() {
//            @Override
//            public void onResponse(@NonNull Call<ArrayList<Category>> call,
//                                   @NonNull Response<ArrayList<Category>> response) {
//                if (response.isSuccessful()) {
//                    mView.updateViewPager(response.body());
//                    mView.showCategories();
//                } else {
//                    mView.updateViewPager(null);
//                    mView.showNoCatgeories();
//                    mView.showRetryCard(mView.getResources().getString(R.string.server_prblm_retry));
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ArrayList<Category>> call, @NonNull Throwable t) {
//                mView.updateViewPager(null);
//                mView.showNoCatgeories();
//                mView.showRetryCard(mView.getResources().getString(R.string.net_prblm_retry));
//            }
//        });

        mView.updateViewPager(categories);

        if (categories != null) mView.showCategories();
        else mView.showNoCatgeories();
    }

    @Override
    public void updateRecyclerView() {
        popularRingtones = getPopular();
        recentRingtones = getRecent();

        //TODO: Comment Out the Server Call
        //TODO: Remove Dummy Data

//        Call<ArrayList<Ringtone>> call = apiService.getRingtones(Config.TOKEN, "recent");
//        call.enqueue(new Callback<ArrayList<Ringtone>>() {
//            @Override
//            public void onResponse(@NonNull Call<ArrayList<Ringtone>> call,
//                                   @NonNull Response<ArrayList<Ringtone>> response) {
//                if (response.isSuccessful()) {
//                    mView.updateRecyclerView(response.body(), "recent");
//                    mView.showRingtones();
//                } else {
//                    mView.updateRecyclerView(null, "recent");
//                    mView.showNoRingtones();
//                    mView.showRetryCard(mView.getResources().getString(R.string.server_prblm_retry));
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ArrayList<Ringtone>> call, @NonNull Throwable t) {
//                mView.updateRecyclerView(null, "recent");
//                mView.showNoRingtones();
//                mView.showRetryCard(mView.getResources().getString(R.string.net_prblm_retry));
//            }
//        });
//
//        Call<ArrayList<Ringtone>> call1 = apiService.getRingtones(Config.TOKEN, "popular");
//        call1.enqueue(new Callback<ArrayList<Ringtone>>() {
//            @Override
//            public void onResponse(@NonNull Call<ArrayList<Ringtone>> call,
//                                   @NonNull Response<ArrayList<Ringtone>> response) {
//                if (response.isSuccessful()) {
//                    mView.updateRecyclerView(response.body(), "popular");
//                    mView.showRingtones();
//                } else {
//                    mView.updateRecyclerView(null, "popular");
//                    mView.showNoRingtones();
//                    mView.showRetryCard(mView.getResources().getString(R.string.server_prblm_retry));
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ArrayList<Ringtone>> call, @NonNull Throwable t) {
//                mView.updateRecyclerView(null, "popular");
//                mView.showNoRingtones();
//                mView.showRetryCard(mView.getResources().getString(R.string.net_prblm_retry));
//            }
//        });

        mView.updateRecyclerView(popularRingtones, "popular");
        mView.updateRecyclerView(recentRingtones, "recent");

        if (popularRingtones != null && recentRingtones != null) mView.showRingtones();
        else mView.showNoRingtones();
    }

    @Override
    public ArrayList<Category> getCategories() {
        String json = null;
        try {
            InputStream is = mView.getAssets().open("categories.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        Category[] collectionItem = new Gson().fromJson(json, Category[].class);
        return new ArrayList<Category>(Arrays.asList(collectionItem));
    }

    @Override
    public ArrayList<Ringtone> getPopular() {
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
    public ArrayList<Ringtone> getRecent() {
        return getPopular();
    }

    @Override
    public String getUserName() {
        return sharedPreferencesHelper.getUserName();
    }

    @Override
    public void searchRingtone(String query) {
        Intent goToRingtones = new Intent(mView, RingtonesActivity.class);

        //Putting the Extras
        goToRingtones.putExtra("name", query);
        goToRingtones.putExtra("mode", "search");
        goToRingtones.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mView.startActivity(goToRingtones);
    }

    @Override
    public void saveRingtone(String type) {
        Log.d(TAG, "saveRingtone: Saving Ringtone");

        String downloadAudioPath = getPath();

        if (fileExists(downloadAudioPath))
            Toast.makeText(mView, "Ringtone Already Downloaded", Toast.LENGTH_SHORT).show();
        else new DownloadFile().execute(ringtone.getRingtoneUrl(), downloadAudioPath, type);
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
    public int dipToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mView.getResources().getDisplayMetrics());
    }

    @Override
    public void setRingtoneObject(String mode, int position) {
        if (mode.equals("recent"))
            ringtone = recentRingtones.get(position);
        else ringtone = popularRingtones.get(position);
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
