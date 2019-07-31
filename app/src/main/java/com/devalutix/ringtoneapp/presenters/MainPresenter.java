package com.devalutix.ringtoneapp.presenters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
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

public class MainPresenter implements MainContract.Presenter {
    private static String TAG = "MainPresenter";

    //Declarations
    private MainActivity mView;
    private PermissionUtil mPermissionUtil;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private GDPR gdpr;
    private ApiEndpointInterface apiService;
    private int position = -1;
    private String mode = null;
    private ArrayList<Ringtone> popularRingtones;
    private ArrayList<Ringtone> recentRingtones;

    //Constructor
    public MainPresenter(PermissionUtil mPermissionUtil, SharedPreferencesHelper sharedPreferencesHelper,
                         GDPR gdpr, ApiEndpointInterface apiService) {
        this.mPermissionUtil = mPermissionUtil;
        this.sharedPreferencesHelper = sharedPreferencesHelper;
        this.gdpr = gdpr;
        this.apiService = apiService;
    }

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
    public void saveRingtone() {
        Log.d(TAG, "saveRingtone: Saving Ringtone");

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Ringtones/" + mView.getResources().getString(R.string.app_name));
        myDir.mkdirs();

        String downloadAudioPath = root + File.separator + getFileName();

        new DownloadFile().execute(getSoundUrl(), downloadAudioPath);
    }

    private String getFileName() {
        String name = "";
        switch (mode) {
            case "recent":
                name = recentRingtones.get(position).getRingtoneTitle();
                break;
            case "popular":
                name = popularRingtones.get(position).getRingtoneTitle();
                break;
        }
        return name;
    }

    private String getSoundUrl() {
        String url = "";
        switch (mode) {
            case "recent":
                url = recentRingtones.get(position).getRingtoneUrl();
                break;
            case "popular":
                url = popularRingtones.get(position).getRingtoneUrl();
                break;
        }
        return url;
    }

    @Override
    public void shareRingtone() {
        Toast.makeText(mView, "Sharing Ringtone", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setAsRingtone() {
        Toast.makeText(mView, "Setting As Ringtone", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setAsNotification() {
        Toast.makeText(mView, "Setting As Notification", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setAsContactRingtone() {
        Toast.makeText(mView, "Setting As Contact Ringtone", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int dipToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mView.getResources().getDisplayMetrics());
    }

    @Override
    public void setCurrent(int position) {
        this.position = position;
    }

    @Override
    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String getMode(){
        return mode;
    }

    @Override
    public int getPosition(){
        return position;
    }

    private class DownloadFile extends AsyncTask<String, Integer, String> {
        int count;

        @Override
        protected String doInBackground(String... url) {
            try {
                URL urls = new URL(url[0]);
                URLConnection connection = urls.openConnection();
                connection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(urls.openStream());
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
            } catch (Exception e) {
            }
            return null;
        }
    }
}
