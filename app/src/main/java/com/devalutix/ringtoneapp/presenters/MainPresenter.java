package com.devalutix.ringtoneapp.presenters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.devalutix.ringtoneapp.contracts.MainContract;
import com.devalutix.ringtoneapp.models.SharedPreferencesHelper;
import com.devalutix.ringtoneapp.ui.activities.MainActivity;
import com.devalutix.ringtoneapp.utils.GDPR;
import com.devalutix.ringtoneapp.utils.PermissionUtil;
import com.google.android.gms.ads.AdView;

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
}
