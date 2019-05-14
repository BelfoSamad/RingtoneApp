package com.devalutix.ringtoneapp.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;

import com.devalutix.ringtoneapp.R;
import com.devalutix.ringtoneapp.contracts.MainContract;
import com.devalutix.ringtoneapp.di.components.DaggerMVPComponent;
import com.devalutix.ringtoneapp.di.components.MVPComponent;
import com.devalutix.ringtoneapp.di.modules.ApplicationModule;
import com.devalutix.ringtoneapp.di.modules.MVPModule;
import com.devalutix.ringtoneapp.presenters.MainPresenter;
import com.devalutix.ringtoneapp.utils.Config;
import com.google.android.gms.ads.AdView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainContract.View {
    private static String TAG = "MainActivity";
    private static final int REQUEST_WRITE_STORAGE = 1;

    //Declarations
    private MVPComponent mvpComponent;
    @Inject
    MainPresenter mPresenter;

    //View Declarations
    //@BindView(R.id.adView_main)
    AdView ad;

    //Listeners

    //Essential Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Creating Views");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Dagger For Application
        mvpComponent = getComponent();

        //Inject the Component Here
        mvpComponent.inject(this);

        //Set ButterKnife
        ButterKnife.bind(this);

        //Attach View To Presenter
        mPresenter.attach(this);

        //Request Storage Permission
        mPresenter.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_STORAGE);

        //Init Ad Banner
        initAdBanner();

        //Search Bar Event Open/Close
        initSearchBar();
    }

    @Override
    public MVPComponent getComponent() {
        if (mvpComponent == null) {
            mvpComponent = DaggerMVPComponent
                    .builder()
                    .applicationModule(new ApplicationModule(getApplication()))
                    .mVPModule(new MVPModule(this))
                    .build();
        }
        return mvpComponent;
    }

    @Override
    protected void onPause() {
        super.onPause();
        AdView ad1;
        if (Config.ENABLE_AD_BANNER) {
            if (Config.ENABLE_GDPR) {
                //If Gdpr enabled get the Variable banner ad from the Class Gdpr
                //else use the current one
                ad1 = mPresenter.getGDPR().getmAd();
            } else ad1 = ad;
            ad1.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdView ad1;
        if (Config.ENABLE_AD_BANNER) {
            if (Config.ENABLE_GDPR) {
                //If Gdpr enabled get the Variable banner ad from the Class Gdpr
                //else use the current one
                ad1 = mPresenter.getGDPR().getmAd();
            } else ad1 = ad;
            ad1.destroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdView ad1;
        if (Config.ENABLE_AD_BANNER) {
            if (Config.ENABLE_GDPR) {
                //If Gdpr enabled get the Variable banner ad from the Class Gdpr
                //else use the current one
                ad1 = mPresenter.getGDPR().getmAd();
            } else ad1 = ad;
            ad1.resume();
        }
    }

    @Override
    public void initViewPager() {

    }

    @Override
    public void initAdBanner() {

    }

    @Override
    public void initSearchBar() {

    }

    @Override
    public void hideAll() {

    }

    @Override
    public void enableTabAt(int i) {

    }
}
