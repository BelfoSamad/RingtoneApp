package com.devalutix.ringtoneapp.di.components;


import android.content.Context;

import com.devalutix.ringtoneapp.ui.activities.CreateOneActivity;
import com.devalutix.ringtoneapp.ui.activities.MainActivity;
import com.devalutix.ringtoneapp.di.annotations.ActivityContext;
import com.devalutix.ringtoneapp.di.modules.ApplicationModule;
import com.devalutix.ringtoneapp.di.modules.MVPModule;
import com.devalutix.ringtoneapp.ui.activities.RingtonesActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, MVPModule.class})
public interface MVPComponent {

    //Inject in Activities
    void inject(MainActivity mainActivity);
    void inject(RingtonesActivity ringtonesActivity);
    void inject(CreateOneActivity createOneActivity);

    //Context
    @ActivityContext
    Context getActivityContext();
}
