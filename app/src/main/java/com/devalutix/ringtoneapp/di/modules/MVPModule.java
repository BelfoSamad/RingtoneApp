package com.devalutix.ringtoneapp.di.modules;

import android.app.Activity;
import android.content.Context;

import com.devalutix.ringtoneapp.di.annotations.ActivityContext;
import com.devalutix.ringtoneapp.models.SharedPreferencesHelper;
import com.devalutix.ringtoneapp.presenters.CreateOnePresenter;
import com.devalutix.ringtoneapp.presenters.MainPresenter;
import com.devalutix.ringtoneapp.presenters.RingtonesPresenter;
import com.devalutix.ringtoneapp.utils.ApiEndpointInterface;
import com.devalutix.ringtoneapp.utils.GDPR;
import com.devalutix.ringtoneapp.utils.PermissionUtil;
import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MVPModule {

    private Activity mActivity;

    //Constructor
    public MVPModule(Activity mActivity) {
        this.mActivity = mActivity;
    }

    //Context
    @Provides
    @ActivityContext
    Context provideContext() {
        return mActivity;
    }

    @Provides
    Activity provideActivity() {
        return mActivity;
    }

    //Presenters
    @Provides
    @Singleton
    MainPresenter providesMainPresenter(SharedPreferencesHelper sharedPreferencesHelper,
                                        PermissionUtil util,
                                        GDPR gdpr,
                                        ApiEndpointInterface apiService){
        return new MainPresenter(util,sharedPreferencesHelper,gdpr, apiService);
    }

    @Provides
    @Singleton
    RingtonesPresenter providesRingtonesPresenter(ApiEndpointInterface apiService){
        return new RingtonesPresenter(apiService);
    }

    @Provides
    @Singleton
    CreateOnePresenter providesCreateOnePresenter(){
        return new CreateOnePresenter();
    }
}
