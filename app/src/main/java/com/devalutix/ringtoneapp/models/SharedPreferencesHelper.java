package com.devalutix.ringtoneapp.models;


import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import java.util.Arrays;


public class SharedPreferencesHelper {
    private static final String TAG = "SharedPreferenceHelper";
    private static final String CATEGORIES = "Categories";
    private static final String COLLECTIONS = "Collections";

    //Declarations
    private SharedPreferences sharedPref;
    private Gson gson;

    //Constructor
    public SharedPreferencesHelper(SharedPreferences sharedPref, Gson gson) {
        this.sharedPref = sharedPref;
        this.gson = gson;
    }

    //Methods

    //Extra Functions
    public void firstTimeAskingPermission(String permission) {
        Log.d(TAG, "firstTimeAskingPermission: check");
        SharedPreferences.Editor editor;
        editor = sharedPref.edit();
        editor.putBoolean(permission, false).apply();
    }

    public boolean isFirstTimeAskingPermission(String permission) {
        Log.d(TAG, "isFirstTimeAskingPermission.");
        return sharedPref != null && sharedPref.getBoolean(permission, true);
    }

    /**
     * Set Download Enabled if user Provides the Permission
     */
    public void setDownloadEnable(boolean enable) {
        SharedPreferences.Editor editor;
        editor = sharedPref.edit();
        editor.putBoolean("Download", enable).apply();
    }

    /**
     * Check if Downloading Option Available
     */
    public boolean isDownloadEnable() {
        return sharedPref.getBoolean("Download", false);
    }
}
