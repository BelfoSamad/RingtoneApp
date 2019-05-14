package com.devalutix.ringtoneapp.contracts;

import com.devalutix.ringtoneapp.base.BasePresenter;
import com.devalutix.ringtoneapp.base.BaseView;
import com.devalutix.ringtoneapp.utils.GDPR;
import com.google.android.gms.ads.AdView;

public interface MainContract {

    interface Presenter extends BasePresenter<View> {

        void requestPermission(String writeExternalStorage, int requestWriteStorage);

        void initGDPR(AdView ad);

        GDPR getGDPR();
    }

    interface View extends BaseView {

        void initViewPager();

        void initAdBanner();

        void initSearchBar();

        void hideAll();

        void enableTabAt(int i);
    }
}
