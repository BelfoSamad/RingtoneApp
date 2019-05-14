package com.devalutix.ringtoneapp.presenters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.util.TypedValue;

import androidx.core.app.ActivityCompat;

import com.devalutix.ringtoneapp.contracts.MainContract;
import com.devalutix.ringtoneapp.models.SharedPreferencesHelper;
import com.devalutix.ringtoneapp.pojo.Category;
import com.devalutix.ringtoneapp.pojo.Ringtone;
import com.devalutix.ringtoneapp.ui.activities.MainActivity;
import com.devalutix.ringtoneapp.utils.GDPR;
import com.devalutix.ringtoneapp.utils.PermissionUtil;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class MainPresenter implements MainContract.Presenter {
    private static String TAG = "MainPresenter";

    //Declarations
    private MainActivity mView;
    private PermissionUtil mPermissionUtil;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private GDPR gdpr;

    //Constructor
    public MainPresenter(PermissionUtil mPermissionUtil, SharedPreferencesHelper sharedPreferencesHelper,
                         GDPR gdpr) {
        this.mPermissionUtil = mPermissionUtil;
        this.sharedPreferencesHelper = sharedPreferencesHelper;
        this.gdpr = gdpr;
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
    public void initGDPR(AdView ad) {
        gdpr.setAd(ad);
        gdpr.checkForConsent();
    }

    @Override
    public GDPR getGDPR() {
        return gdpr;
    }

    @Override
    public void initViewPager() {
        ArrayList<Category> categories = getCategories();

        mView.initViewPager(categories);

        if (categories != null) mView.showCategories();
        else mView.showNoCatgeories();
    }

    @Override
    public void initRecyclerView() {
        ArrayList<Ringtone> popular = getPopular();
        ArrayList<Ringtone> recent = getRecent();

        mView.initRecyclerView(popular, "popular");
        mView.initRecyclerView(recent, "recent");

        if (popular != null && recent != null) mView.showRingtones();
        else mView.hideRingtones();
    }

    @Override
    public void updateAll() {

    }

    @Override
    public ArrayList<Category> getCategories() {
        //TODO: Get Categories from Server

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
        //TODO: Get Popular Ringtones from Server
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
        //TODO: Get Recent Ringtones from Server
        return getPopular();
    }

    @Override
    public String getUserName() {
        return sharedPreferencesHelper.getUserName();
    }

    @Override
    public int dipToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mView.getResources().getDisplayMetrics());
    }
}
