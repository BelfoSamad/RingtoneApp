package com.devalutix.ringtoneapp.di.components;

import android.content.Context;

import com.devalutix.ringtoneapp.base.BaseApplication;
import com.devalutix.ringtoneapp.di.annotations.ApplicationContext;
import com.devalutix.ringtoneapp.di.modules.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

/**********************************
 © 2018 Sam Dev
 ALL RIGHTS RESERVED
 ***********************************/

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(BaseApplication baseApplication);

    /*
        Put Here All the Dependencies The Application Provides
     */

    //Context
    @ApplicationContext
    Context getApplicationContext();
}
